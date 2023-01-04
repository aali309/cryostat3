/*
 * Copyright The Cryostat Authors
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or data
 * (collectively the "Software"), free of charge and under any and all copyright
 * rights in the Software, and any and all patent rights owned or freely
 * licensable by each licensor hereunder covering either (i) the unmodified
 * Software as contributed to or provided by such licensor, or (ii) the Larger
 * Works (as defined below), to deal in both
 *
 * (a) the Software, and
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software (each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 * The above copyright notice and either this complete permission notice or at
 * a minimum a reference to the UPL must be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.cryostat.recordings;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;

import org.openjdk.jmc.common.unit.IConstrainedMap;
import org.openjdk.jmc.common.unit.IOptionDescriptor;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.configuration.events.EventOptionID;
import org.openjdk.jmc.flightrecorder.configuration.recording.RecordingOptionsBuilder;
import org.openjdk.jmc.rjmx.services.jfr.FlightRecorderException;
import org.openjdk.jmc.rjmx.services.jfr.IEventTypeInfo;
import org.openjdk.jmc.rjmx.services.jfr.IFlightRecorderService;
import org.openjdk.jmc.rjmx.services.jfr.IRecordingDescriptor;

import io.cryostat.core.net.JFRConnection;
import io.cryostat.core.templates.Template;
import io.cryostat.core.templates.TemplateType;
import io.cryostat.targets.Target;
import io.cryostat.targets.TargetConnectionManager;
import io.cryostat.ws.MessagingServer;
import io.cryostat.ws.Notification;

import io.vertx.core.eventbus.EventBus;
import jdk.jfr.RecordingState;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("")
public class Recordings {

    private static final Pattern TEMPLATE_PATTERN =
            Pattern.compile("^template=([\\w]+)(?:,type=([\\w]+))?$");

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject TargetConnectionManager connectionManager;
    @Inject RecordingOptionsBuilderFactory recordingOptionsBuilderFactory;
    @Inject EventOptionsBuilder.Factory eventOptionsBuilderFactory;
    @Inject EventBus bus;

    @GET
    @Path("/api/v1/recordings")
    @RolesAllowed("recording:read")
    public List<ArchivedRecording> listArchives() {
        return List.of();
    }

    @GET
    @Path("/api/v3/targets/{id}/recordings")
    @RolesAllowed({"recording:read", "target:read"})
    public List<LinkedRecordingDescriptor> listForTarget(@RestPath long id) throws Exception {
        Target target = Target.findById(id);
        if (target == null) {
            throw new NotFoundException();
        }
        return target.activeRecordings.stream().map(this::mapDescriptor).toList();
    }

    @GET
    @Path("/api/v1/targets/{connectUrl}/recordings")
    @RolesAllowed({"recording:read", "target:read"})
    public List<LinkedRecordingDescriptor> listForTargetByUrl(@RestPath URI connectUrl)
            throws Exception {
        Target target = Target.getTargetByConnectUrl(connectUrl);
        return listForTarget(target.id);
    }

    @Transactional
    @POST
    @Path("/api/v3/targets/{id}/recordings")
    @RolesAllowed({"recording:create", "target:read", "target:update"})
    public LinkedRecordingDescriptor createRecording(
            @RestPath long id,
            @RestForm String recordingName,
            @RestForm String events,
            @RestForm Optional<Long> duration,
            @RestForm Optional<Boolean> toDisk,
            @RestForm Optional<Long> maxAge,
            @RestForm Optional<Long> maxSize,
            @RestForm Optional<String> metadata,
            @RestForm Optional<Boolean> archiveOnStop)
            throws Exception {
        if (StringUtils.isBlank(recordingName)) {
            throw new BadRequestException("\"recordingName\" form parameter must be provided");
        }
        if (StringUtils.isBlank(events)) {
            throw new BadRequestException("\"events\" form parameter must be provided");
        }

        Target target = Target.findById(id);
        LinkedRecordingDescriptor descriptor =
                connectionManager.executeConnectedTask(
                        target,
                        connection -> {
                            Optional<IRecordingDescriptor> previous =
                                    getDescriptorByName(connection, recordingName);
                            if (previous.isPresent()) {
                                throw new BadRequestException(
                                        String.format(
                                                "Recording with name \"%s\" already exists",
                                                recordingName));
                            }

                            RecordingOptionsBuilder optionsBuilder =
                                    recordingOptionsBuilderFactory
                                            .create(connection.getService())
                                            .name(recordingName);
                            if (duration.isPresent()) {
                                optionsBuilder.duration(duration.get());
                            }
                            if (toDisk.isPresent()) {
                                optionsBuilder.toDisk(toDisk.get());
                            }
                            if (maxAge.isPresent()) {
                                optionsBuilder.maxAge(maxAge.get());
                            }
                            if (maxSize.isPresent()) {
                                optionsBuilder.maxSize(maxSize.get());
                            }
                            // if (attrs.contains("metadata")) {
                            //     metadata =
                            //             gson.fromJson(
                            //                     attrs.get("metadata"),
                            //                     new TypeToken<Metadata>() {}.getType());
                            // }
                            // boolean archiveOnStop = false;
                            // if (attrs.contains("archiveOnStop")) {
                            //     if (attrs.get("archiveOnStop").equals("true")
                            //             || attrs.get("archiveOnStop").equals("false")) {
                            //         archiveOnStop = Boolean.valueOf(attrs.get("archiveOnStop"));
                            //     } else {
                            //         throw new HttpException(400, "Invalid options");
                            //     }
                            // }
                            IConstrainedMap<String> recordingOptions = optionsBuilder.build();

                            Pair<String, TemplateType> template =
                                    parseEventSpecifierToTemplate(events);
                            String templateName = template.getKey();
                            TemplateType templateType = template.getValue();
                            TemplateType preferredTemplateType =
                                    getPreferredTemplateType(
                                            connection, templateName, templateType);

                            IRecordingDescriptor desc =
                                    connection
                                            .getService()
                                            .start(
                                                    recordingOptions,
                                                    enableEvents(
                                                            connection,
                                                            templateName,
                                                            preferredTemplateType));

                            Metadata meta =
                                    new Metadata(
                                            Map.of(
                                                    "template.name",
                                                    templateName,
                                                    "template.type",
                                                    preferredTemplateType.name()));
                            return new LinkedRecordingDescriptor(
                                    desc.getId(),
                                    mapState(desc),
                                    desc.getDuration().in(UnitLookup.MILLISECOND).longValue(),
                                    desc.getStartTime().in(UnitLookup.EPOCH_MS).longValue(),
                                    desc.isContinuous(),
                                    desc.getToDisk(),
                                    desc.getMaxSize().in(UnitLookup.BYTE).longValue(),
                                    desc.getMaxAge().in(UnitLookup.MILLISECOND).longValue(),
                                    desc.getName(),
                                    "TODO",
                                    "TODO",
                                    meta);
                        });

        ActiveRecording recording = ActiveRecording.from(target, descriptor);
        recording.persist();
        target.activeRecordings.add(recording);
        target.persist();
        notify(NotificationCategory.ACTIVE_CREATE, target.connectUrl, descriptor);

        // Object fixedDuration =
        //         recordingOptions.get(RecordingOptionsBuilder.KEY_DURATION);
        // if (fixedDuration != null) {
        //     Long delay =
        //             Long.valueOf(fixedDuration.toString().replaceAll("[^0-9]", ""));

        //     scheduleRecordingTasks(
        //             recordingName, delay, connectionDescriptor, archiveOnStop);
        // }

        return descriptor;
    }

    private Optional<IRecordingDescriptor> getDescriptorById(JFRConnection connection, long id) {
        try {
            return connection.getService().getAvailableRecordings().stream()
                    .filter(r -> id == r.getId())
                    .findFirst();
        } catch (Exception e) {
            logger.error("Target connection failed", e);
            throw new ServerErrorException(500, e);
        }
    }

    private Optional<IRecordingDescriptor> getDescriptorByName(
            JFRConnection connection, String recordingName) {
        try {
            return connection.getService().getAvailableRecordings().stream()
                    .filter(r -> Objects.equals(r.getName(), recordingName))
                    .findFirst();
        } catch (Exception e) {
            logger.error("Target connection failed", e);
            throw new ServerErrorException(500, e);
        }
    }

    @Transactional
    @POST
    @Path("/api/v1/targets/{connectUrl}/recordings")
    @RolesAllowed({"recording:create", "target:read", "target:update"})
    public LinkedRecordingDescriptor createRecordingV1(
            @RestPath URI connectUrl,
            @RestForm String recordingName,
            @RestForm String events,
            @RestForm Optional<Long> duration,
            @RestForm Optional<Boolean> toDisk,
            @RestForm Optional<Long> maxAge,
            @RestForm Optional<Long> maxSize,
            @RestForm Optional<String> metadata,
            @RestForm Optional<Boolean> archiveOnStop)
            throws Exception {
        Target target = Target.getTargetByConnectUrl(connectUrl);
        return createRecording(
                target.id,
                recordingName,
                events,
                maxSize,
                archiveOnStop,
                maxSize,
                maxSize,
                metadata,
                archiveOnStop);
    }

    @Transactional
    @DELETE
    @Path("/api/v1/targets/{connectUrl}/recordings/{recordingName}")
    @RolesAllowed({"recording:delete", "target:read", "target:update"})
    public void deleteRecordingV1(@RestPath URI connectUrl, @RestPath String recordingName)
            throws Exception {
        if (StringUtils.isBlank(recordingName)) {
            throw new BadRequestException("\"recordingName\" form parameter must be provided");
        }
        Target target = Target.getTargetByConnectUrl(connectUrl);
        target.activeRecordings.stream()
                .filter(r -> Objects.equals(r.name, recordingName))
                .findFirst()
                .ifPresentOrElse(
                        r -> {
                            try {
                                connectionManager.executeConnectedTask(
                                        target,
                                        conn -> {
                                            getDescriptorById(conn, r.remoteId)
                                                    .ifPresent(
                                                            rec -> safeCloseRecording(conn, rec));
                                            return null;
                                        });
                            } catch (WebApplicationException e) {
                                throw e;
                            } catch (Exception e) {
                                throw new ServerErrorException(
                                        "Failed to stop remote recording", 500, e);
                            }
                            r.delete();
                            target.activeRecordings.remove(r);
                            notify(
                                    NotificationCategory.ACTIVE_DELETE,
                                    connectUrl,
                                    mapDescriptor(r));
                        },
                        () -> {
                            throw new NotFoundException();
                        });
    }

    @Transactional
    @DELETE
    @Path("/api/v3/targets/{targetId}/recordings/{remoteId}")
    @RolesAllowed({"recording:delete", "target:read", "target:update"})
    public void deleteRecording(@RestPath long targetId, @RestPath long remoteId) throws Exception {
        Target target = Target.findById(targetId);
        target.activeRecordings.stream()
                .filter(r -> r.remoteId == remoteId)
                .findFirst()
                .ifPresentOrElse(
                        r -> {
                            try {
                                connectionManager.executeConnectedTask(
                                        target,
                                        conn -> {
                                            getDescriptorById(conn, r.remoteId)
                                                    .ifPresent(
                                                            rec -> safeCloseRecording(conn, rec));
                                            return null;
                                        });
                            } catch (WebApplicationException e) {
                                throw e;
                            } catch (Exception e) {
                                logger.error("Failed to stop remote recording", e);
                                throw new ServerErrorException(
                                        "Failed to stop remote recording", 500, e);
                            }
                            r.delete();
                            target.activeRecordings.remove(r);
                            notify(
                                    NotificationCategory.ACTIVE_DELETE,
                                    target.connectUrl,
                                    mapDescriptor(r));
                        },
                        () -> {
                            throw new NotFoundException();
                        });
    }

    private void safeCloseRecording(JFRConnection conn, IRecordingDescriptor rec) {
        try {
            conn.getService().close(rec);
        } catch (FlightRecorderException e) {
            logger.error("Failed to stop remote" + " recording", e);
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }
    }

    @GET
    @Path("/api/v1/targets/{connectUrl}/recordingOptions")
    @RolesAllowed("target:read")
    public Map<String, Object> getRecordingOptionsV1(@RestPath URI connectUrl) throws Exception {
        Target target = Target.getTargetByConnectUrl(connectUrl);
        return getRecordingOptions(target.id);
    }

    @GET
    @Path("/api/v3/targets/{id}/recordingOptions")
    @RolesAllowed("target:read")
    public Map<String, Object> getRecordingOptions(@RestPath long id) throws Exception {
        Target target = Target.findById(id);
        return connectionManager.executeConnectedTask(
                target,
                connection -> {
                    RecordingOptionsBuilder builder =
                            recordingOptionsBuilderFactory.create(connection.getService());
                    return getRecordingOptions(connection.getService(), builder);
                });
    }

    private static Map<String, Object> getRecordingOptions(
            IFlightRecorderService service, RecordingOptionsBuilder builder) throws Exception {
        IConstrainedMap<String> recordingOptions = builder.build();

        Map<String, IOptionDescriptor<?>> targetRecordingOptions =
                service.getAvailableRecordingOptions();

        Map<String, Object> map = new HashMap<String, Object>();

        if (recordingOptions.get("toDisk") != null) {
            map.put("toDisk", recordingOptions.get("toDisk"));
        } else {
            map.put("toDisk", targetRecordingOptions.get("disk").getDefault());
        }

        map.put("maxAge", getNumericOption("maxAge", recordingOptions, targetRecordingOptions));
        map.put("maxSize", getNumericOption("maxSize", recordingOptions, targetRecordingOptions));

        return map;
    }

    private static Long getNumericOption(
            String name,
            IConstrainedMap<String> defaultOptions,
            Map<String, IOptionDescriptor<?>> targetOptions) {
        Object value;

        if (defaultOptions.get(name) != null) {
            value = defaultOptions.get(name);
        } else {
            value = targetOptions.get(name).getDefault();
        }

        if (value instanceof Number) {
            return Long.valueOf(((Number) value).longValue());
        }
        return null;
    }

    private RecordingState mapState(IRecordingDescriptor desc) {
        switch (desc.getState()) {
            case CREATED:
                return RecordingState.NEW;
            case RUNNING:
                return RecordingState.RUNNING;
            case STOPPING:
                return RecordingState.RUNNING;
            case STOPPED:
                return RecordingState.STOPPED;
            default:
                logger.warn("Unrecognized recording state: {}", desc.getState());
                return RecordingState.CLOSED;
        }
    }

    private LinkedRecordingDescriptor mapDescriptor(ActiveRecording desc) {
        return new LinkedRecordingDescriptor(
                desc.remoteId,
                desc.state,
                desc.duration,
                desc.startTime,
                desc.continuous,
                desc.toDisk,
                desc.maxSize,
                desc.maxAge,
                desc.name,
                "TODO",
                "TODO",
                desc.metadata);
    }

    private static Pair<String, TemplateType> parseEventSpecifierToTemplate(String eventSpecifier) {
        if (TEMPLATE_PATTERN.matcher(eventSpecifier).matches()) {
            Matcher m = TEMPLATE_PATTERN.matcher(eventSpecifier);
            m.find();
            String templateName = m.group(1);
            String typeName = m.group(2);
            TemplateType templateType = null;
            if (StringUtils.isNotBlank(typeName)) {
                templateType = TemplateType.valueOf(typeName.toUpperCase());
            }
            return Pair.of(templateName, templateType);
        }
        throw new BadRequestException(eventSpecifier);
    }

    private static TemplateType getPreferredTemplateType(
            JFRConnection connection, String templateName, TemplateType templateType)
            throws Exception {
        if (templateType != null) {
            return templateType;
        }
        if (templateName.equals("ALL")) {
            // special case for the ALL meta-template
            return TemplateType.TARGET;
        }
        List<Template> matchingNameTemplates =
                connection.getTemplateService().getTemplates().stream()
                        .filter(t -> t.getName().equals(templateName))
                        .toList();
        boolean custom =
                matchingNameTemplates.stream()
                        .anyMatch(t -> t.getType().equals(TemplateType.CUSTOM));
        if (custom) {
            return TemplateType.CUSTOM;
        }
        boolean target =
                matchingNameTemplates.stream()
                        .anyMatch(t -> t.getType().equals(TemplateType.TARGET));
        if (target) {
            return TemplateType.TARGET;
        }
        throw new BadRequestException(
                String.format("Invalid/unknown event template %s", templateName));
    }

    private IConstrainedMap<EventOptionID> enableEvents(
            JFRConnection connection, String templateName, TemplateType templateType)
            throws Exception {
        if (templateName.equals("ALL")) {
            return enableAllEvents(connection);
        }
        // if template type not specified, try to find a Custom template by that name. If none,
        // fall back on finding a Target built-in template by the name. If not, throw an
        // exception and bail out.
        TemplateType type = getPreferredTemplateType(connection, templateName, templateType);
        return connection.getTemplateService().getEvents(templateName, type).get();
    }

    private IConstrainedMap<EventOptionID> enableAllEvents(JFRConnection connection)
            throws Exception {
        EventOptionsBuilder builder = eventOptionsBuilderFactory.create(connection);

        for (IEventTypeInfo eventTypeInfo : connection.getService().getAvailableEventTypes()) {
            builder.addEvent(eventTypeInfo.getEventTypeID().getFullKey(), "enabled", "true");
        }

        return builder.build();
    }

    private void notify(NotificationCategory category, URI connectUrl, Object recording) {
        bus.publish(
                MessagingServer.class.getName(),
                new Notification(category.cat, new RecordingEvent(connectUrl, recording)));
    }

    private enum NotificationCategory {
        ACTIVE_CREATE("ActiveRecordingCreated"),
        ACTIVE_STOP("ActiveRecordingStopped"),
        ACTIVE_DELETE("ActiveRecordingDeleted"),
        SNAPSHOT_CREATE("SnapshotCreated"),
        SNAPSHOT_DELETE("SnapshotDeleted"),
        ;

        private final String cat;

        NotificationCategory(String cat) {
            this.cat = cat;
        }
    }

    private record RecordingEvent(URI target, Object recording) {}

    public record LinkedRecordingDescriptor(
            long id,
            RecordingState state,
            long duration,
            long startTime,
            boolean continuous,
            boolean toDisk,
            long maxSize,
            long maxAge,
            String name,
            String downloadUrl,
            String reportUrl,
            Metadata metadata) {
        public static LinkedRecordingDescriptor from(ActiveRecording recording) {
            return new LinkedRecordingDescriptor(
                    recording.remoteId,
                    recording.state,
                    recording.duration,
                    recording.startTime,
                    recording.continuous,
                    recording.toDisk,
                    recording.maxSize,
                    recording.maxAge,
                    recording.name,
                    "TODO",
                    "TODO",
                    recording.metadata);
        }
    }

    public record ArchivedRecording(
            String name,
            String downloadUrl,
            String reportUrl,
            Metadata metadata,
            long size,
            long archivedTime) {}

    public record Metadata(Map<String, String> labels) {}
}