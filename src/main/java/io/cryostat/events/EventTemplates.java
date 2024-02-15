/*
 * Copyright The Cryostat Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cryostat.events;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import io.cryostat.core.sys.FileSystem;
=======
=======
import org.jsoup.nodes.Document;
=======
>>>>>>> d522ffe0 (update import, apply spotless)
import org.openjdk.jmc.common.unit.IConstrainedMap;
import org.openjdk.jmc.flightrecorder.configuration.events.EventOptionID;
>>>>>>> a07df122 (tmp)
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.JFCGrammar;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.XMLAttributeInstance;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.XMLModel;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.XMLTagInstance;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.XMLValidationResult;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.model.EventConfiguration;

<<<<<<< HEAD
>>>>>>> 3d3534db (feat(eventtemplates): custom event templates in S3)
import io.cryostat.core.templates.MutableTemplateService.InvalidEventTemplateException;
import io.cryostat.core.templates.MutableTemplateService.InvalidXmlException;
=======
>>>>>>> d522ffe0 (update import, apply spotless)
import io.cryostat.ConfigProperties;
import io.cryostat.core.FlightRecorderException;
import io.cryostat.core.templates.MutableTemplateService.InvalidEventTemplateException;
import io.cryostat.core.templates.MutableTemplateService.InvalidXmlException;
import io.cryostat.core.templates.Template;
import io.cryostat.core.templates.TemplateService;
import io.cryostat.core.templates.TemplateType;
import io.cryostat.targets.Target;
<<<<<<< HEAD
import io.cryostat.util.HttpMimeType;

import io.smallrye.common.annotation.Blocking;
=======
import io.cryostat.targets.TargetConnectionManager;
import io.cryostat.util.HttpStatusCodeIdentifier;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
>>>>>>> 3d3534db (feat(eventtemplates): custom event templates in S3)
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
<<<<<<< HEAD
=======
import jakarta.ws.rs.NotFoundException;
>>>>>>> 3d3534db (feat(eventtemplates): custom event templates in S3)
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
<<<<<<< HEAD
<<<<<<< HEAD
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jsoup.nodes.Document;
=======
import org.apache.http.entity.ContentType;
=======
import org.apache.hc.core5.http.ContentType;
>>>>>>> d522ffe0 (update import, apply spotless)
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jsoup.nodes.Document;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
>>>>>>> 3d3534db (feat(eventtemplates): custom event templates in S3)

@Path("")
public class EventTemplates {

    public static final Template ALL_EVENTS_TEMPLATE =
            new Template(
                    "ALL",
                    "Enable all available events in the target JVM, with default option values."
                            + " This will be very expensive and is intended primarily for testing"
                            + " Cryostat's own capabilities.",
                    "Cryostat",
                    TemplateType.TARGET);

<<<<<<< HEAD
    @Inject FileSystem fs;
    @Inject TargetTemplateService.Factory targetTemplateServiceFactory;
    @Inject S3TemplateService customTemplateService;
    @Inject Logger logger;
=======
    @Inject Vertx vertx;
    @Inject TargetConnectionManager connectionManager;
    @Inject S3Client storage;
    @Inject Logger logger;

    @ConfigProperty(name = ConfigProperties.AWS_BUCKET_NAME_EVENT_TEMPLATES)
    String eventTemplatesBucket;

    void onStart(@Observes StartupEvent evt) {
        // FIXME refactor this to a reusable utility method since this is done for custom event
        // templates, archived recordings, and archived reports
        boolean exists = false;
        try {
            exists =
                    HttpStatusCodeIdentifier.isSuccessCode(
                            storage.headBucket(
                                            HeadBucketRequest.builder()
                                                    .bucket(eventTemplatesBucket)
                                                    .build())
                                    .sdkHttpResponse()
                                    .statusCode());
        } catch (Exception e) {
            logger.info(e);
        }
        if (!exists) {
            try {
                storage.createBucket(
                        CreateBucketRequest.builder().bucket(eventTemplatesBucket).build());
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }
>>>>>>> 3d3534db (feat(eventtemplates): custom event templates in S3)

    @GET
    @Path("/api/v1/targets/{connectUrl}/templates")
    @RolesAllowed("read")
    public Response listTemplatesV1(@RestPath URI connectUrl) throws Exception {
        Target target = Target.getTargetByConnectUrl(connectUrl);
        return Response.status(RestResponse.Status.PERMANENT_REDIRECT)
                .location(
                        URI.create(String.format("/api/v3/targets/%d/event_templates", target.id)))
                .build();
    }

    @POST
    @Path("/api/v1/templates")
    @RolesAllowed("write")
<<<<<<< HEAD
    public Response postTemplatesV1(@RestForm("template") FileUpload body) {
        return Response.status(RestResponse.Status.PERMANENT_REDIRECT)
                .location(URI.create("/api/v3/event_templates"))
                .build();
    }

    @POST
    @Path("/api/v3/event_templates")
    @RolesAllowed("write")
    public void postTemplates(@RestForm("template") FileUpload body) throws IOException {
        if (body == null || body.filePath() == null || !"template".equals(body.name())) {
            throw new BadRequestException();
        }
        try (var stream = fs.newInputStream(body.filePath())) {
            customTemplateService.addTemplate(stream);
        } catch (InvalidEventTemplateException | InvalidXmlException e) {
            throw new BadRequestException(e);
        }
    }

    @DELETE
    @Path("/api/v1/templates/{templateName}")
    @RolesAllowed("write")
    public Response deleteTemplatesV1(@RestPath String templateName) {
        return Response.status(RestResponse.Status.PERMANENT_REDIRECT)
                .location(URI.create(String.format("/api/v3/event_templates/%s", templateName)))
                .build();
    }

    @DELETE
    @Blocking
    @Path("/api/v3/event_templates/{templateName}")
    @RolesAllowed("write")
    public void deleteTemplates(@RestPath String templateName) {
        customTemplateService.deleteTemplate(templateName);
=======
    public Uni<Void> postTemplatesV1(@RestForm("template") FileUpload body) throws Exception {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        var path = body.filePath();
        vertx.fileSystem()
                .readFile(path.toString())
                .onComplete(
                        ar -> {
                            try {
                                addTemplate(ar.result().toString());
                                cf.complete(null);
                            } catch (Exception e) {
                                logger.error(e);
                                cf.completeExceptionally(e);
                            }
                        })
                .onFailure(
                        ar -> {
                            logger.error(ar.getCause());
                            cf.completeExceptionally(ar.getCause());
                        });
        return Uni.createFrom().future(cf);
>>>>>>> 3d3534db (feat(eventtemplates): custom event templates in S3)
    }

    @GET
    @Path("/api/v1/targets/{connectUrl}/templates/{templateName}/type/{templateType}")
    @RolesAllowed("read")
    public Response getTargetTemplateV1(
            @RestPath URI connectUrl,
            @RestPath String templateName,
            @RestPath TemplateType templateType) {
        Target target = Target.getTargetByConnectUrl(connectUrl);
        return Response.status(RestResponse.Status.PERMANENT_REDIRECT)
                .location(
                        URI.create(
                                String.format(
                                        "/api/v3/targets/%d/event_templates/%s/%s",
                                        target.id, templateType, templateName)))
                .build();
    }

    @GET
    @Path("/api/v2.1/targets/{connectUrl}/templates/{templateName}/type/{templateType}")
    @RolesAllowed("read")
    public Response getTargetTemplateV2_1(
            @RestPath URI connectUrl,
            @RestPath String templateName,
            @RestPath TemplateType templateType) {
        Target target = Target.getTargetByConnectUrl(connectUrl);
        return Response.status(RestResponse.Status.PERMANENT_REDIRECT)
                .location(
                        URI.create(
                                String.format(
                                        "/api/v3/targets/%d/event_templates/%s/%s",
                                        target.id, templateType, templateName)))
                .build();
    }

    @GET
    @Blocking
    @Path("/api/v3/event_templates")
    @RolesAllowed("read")
    public List<Template> listTemplates() throws Exception {
        var list = new ArrayList<Template>();
        list.add(ALL_EVENTS_TEMPLATE);
        list.addAll(customTemplateService.getTemplates());
        return list;
    }

    @GET
    @Blocking
    @Path("/api/v3/targets/{id}/event_templates")
    @RolesAllowed("read")
    public List<Template> listTargetTemplates(@RestPath long id) throws Exception {
        Target target = Target.find("id", id).singleResult();
        var list = new ArrayList<Template>();
        list.add(ALL_EVENTS_TEMPLATE);
        list.addAll(targetTemplateServiceFactory.create(target).getTemplates());
        list.addAll(customTemplateService.getTemplates());
        return list;
    }

    @GET
    @Blocking
    @Path("/api/v3/targets/{id}/event_templates/{templateType}/{templateName}")
    @RolesAllowed("read")
    public Response getTargetTemplate(
            @RestPath long id, @RestPath TemplateType templateType, @RestPath String templateName)
            throws Exception {
        Target target = Target.find("id", id).singleResult();
        Document doc;
        switch (templateType) {
            case TARGET:
                doc =
                        targetTemplateServiceFactory
                                .create(target)
                                .getXml(templateName, templateType)
                                .orElseThrow();
                break;
            case CUSTOM:
                doc = customTemplateService.getXml(templateName, templateType).orElseThrow();
                break;
            default:
                throw new BadRequestException();
        }
        return Response.status(RestResponse.Status.OK)
                .header(HttpHeaders.CONTENT_TYPE, HttpMimeType.JFC.mime())
                .entity(doc.toString())
                .build();
    }

    static class S3TemplateService implements TemplateService {
        S3Client s3;

        @Override
        public Optional<IConstrainedMap<EventOptionID>> getEvents(
                String templateName, TemplateType templateType) throws FlightRecorderException {
            return Optional.empty();
        }

        @Override
        public List<Template> getTemplates() throws FlightRecorderException {
            var builder = ListObjectsV2Request.builder().bucket(eventTemplatesBucket);
            var objects = s3.listObjectsV2(builder.build());
            var templates = convertObjects(objects);
            return templates;
        }

        private List<Template> convertObjects(ListObjectsV2Response objects) {
            return List.of();
        }

        @Override
        public Optional<Document> getXml(String templateName, TemplateType templateType)
                throws FlightRecorderException {
            return Optional.empty();
        }
    }

    @Blocking
    public Template addTemplate(String templateText)
            throws InvalidXmlException, InvalidEventTemplateException, IOException {
        try {
            XMLModel model = EventConfiguration.createModel(templateText);
            model.checkErrors();

            for (XMLValidationResult result : model.getResults()) {
                if (result.isError()) {
                    // throw new InvalidEventTemplateException(result.getText());
                    throw new IllegalArgumentException(result.getText());
                }
            }

            XMLTagInstance configuration = model.getRoot();
            XMLAttributeInstance labelAttr = null;
            for (XMLAttributeInstance attr : configuration.getAttributeInstances()) {
                if (attr.getAttribute().getName().equals("label")) {
                    labelAttr = attr;
                    break;
                }
            }

            if (labelAttr == null) {
                // throw new InvalidEventTemplateException(
                //         "Template has no configuration label attribute");
                throw new IllegalArgumentException("Template has no configuration label attribute");
            }

            String templateName = labelAttr.getExplicitValue();
            templateName = templateName.replaceAll("[\\W]+", "_");

            XMLTagInstance root = model.getRoot();
            root.setValue(JFCGrammar.ATTRIBUTE_LABEL_MANDATORY, templateName);

            String key = templateName;
            storage.putObject(
                    PutObjectRequest.builder()
                            .bucket(eventTemplatesBucket)
                            .key(key)
                            .contentType(ContentType.APPLICATION_XML.getMimeType())
                            .build(),
                    RequestBody.fromString(model.toString()));

            return new Template(
                    templateName,
                    getAttributeValue(root, "description"),
                    getAttributeValue(root, "provider"),
                    TemplateType.CUSTOM);
        } catch (IOException ioe) {
            // throw new InvalidXmlException("Unable to parse XML stream", ioe);
            throw new IllegalArgumentException("Unable to parse XML stream", ioe);
        } catch (ParseException | IllegalArgumentException e) {
            // throw new InvalidEventTemplateException("Invalid XML", e);
            throw new IllegalArgumentException("Invalid XML", e);
        }
    }

    protected String getAttributeValue(XMLTagInstance node, String valueKey) {
        return node.getAttributeInstances().stream()
                .filter(i -> Objects.equals(valueKey, i.getAttribute().getName()))
                .map(i -> i.getValue())
                .findFirst()
                .get();
    }
}
