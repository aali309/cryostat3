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
<<<<<<< HEAD
<<<<<<< HEAD
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
<<<<<<< HEAD
=======
=======
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
>>>>>>> af9dee6c (tmp)
import java.text.ParseException;
import java.util.List;
>>>>>>> f1bce2df (refactor, split out custom event templates service)
=======
>>>>>>> 1208a8f3 (store attrs as metadata tags)
import java.util.Objects;
import java.util.Optional;

import org.openjdk.jmc.common.unit.IConstrainedMap;
<<<<<<< HEAD
<<<<<<< HEAD
import org.openjdk.jmc.common.unit.SimpleConstrainedMap;
import org.openjdk.jmc.common.unit.UnitLookup;
=======
>>>>>>> f1bce2df (refactor, split out custom event templates service)
=======
import org.openjdk.jmc.common.unit.SimpleConstrainedMap;
import org.openjdk.jmc.common.unit.UnitLookup;
>>>>>>> af9dee6c (tmp)
import org.openjdk.jmc.flightrecorder.configuration.events.EventOptionID;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.JFCGrammar;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.XMLAttributeInstance;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.XMLModel;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.XMLTagInstance;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.configuration.model.xml.XMLValidationResult;
import org.openjdk.jmc.flightrecorder.controlpanel.ui.model.EventConfiguration;

import io.cryostat.ConfigProperties;
<<<<<<< HEAD
<<<<<<< HEAD
import io.cryostat.Producers;
import io.cryostat.StorageBuckets;
import io.cryostat.core.FlightRecorderException;
import io.cryostat.core.templates.MutableTemplateService;
<<<<<<< HEAD
import io.cryostat.core.templates.MutableTemplateService.InvalidEventTemplateException;
import io.cryostat.core.templates.MutableTemplateService.InvalidXmlException;
import io.cryostat.core.templates.Template;
import io.cryostat.core.templates.TemplateType;
import io.cryostat.ws.MessagingServer;
import io.cryostat.ws.Notification;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

@ApplicationScoped
public class S3TemplateService implements MutableTemplateService {

    static final String EVENT_TEMPLATE_CREATED = "TemplateUploaded";
    static final String EVENT_TEMPLATE_DELETED = "TemplateDeleted";

    @ConfigProperty(name = ConfigProperties.AWS_BUCKET_NAME_EVENT_TEMPLATES)
    String bucket;

    @Inject S3Client storage;
    @Inject StorageBuckets storageBuckets;

    @Inject EventBus bus;

    @Inject
    @Named(Producers.BASE64_URL)
    Base64 base64Url;

=======
=======
import io.cryostat.Producers;
>>>>>>> 1208a8f3 (store attrs as metadata tags)
import io.cryostat.core.FlightRecorderException;
=======
>>>>>>> 18935e67 (implement more suitable interface)
import io.cryostat.core.templates.MutableTemplateService.InvalidEventTemplateException;
import io.cryostat.core.templates.MutableTemplateService.InvalidXmlException;
import io.cryostat.core.templates.Template;
import io.cryostat.core.templates.TemplateType;
import io.cryostat.util.HttpStatusCodeIdentifier;
import io.cryostat.ws.MessagingServer;
import io.cryostat.ws.Notification;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

@ApplicationScoped
public class S3TemplateService implements MutableTemplateService {

    static final String EVENT_TEMPLATE_CREATED = "TemplateUploaded";
    static final String EVENT_TEMPLATE_DELETED = "TemplateDeleted";

    @Inject S3Client storage;
<<<<<<< HEAD
>>>>>>> f1bce2df (refactor, split out custom event templates service)
=======

    @Inject EventBus bus;

    @Inject
    @Named(Producers.BASE64_URL)
    Base64 base64Url;

>>>>>>> 1208a8f3 (store attrs as metadata tags)
    @Inject Logger logger;

    void onStart(@Observes StartupEvent evt) {
        storageBuckets.createIfNecessary(bucket);
    }

    @Override
<<<<<<< HEAD
<<<<<<< HEAD
    @Blocking
    public Optional<IConstrainedMap<EventOptionID>> getEvents(
            String templateName, TemplateType unused) throws FlightRecorderException {
        try (var stream = getModel(templateName)) {
            return Optional.of(
                    new EventConfiguration(parseXml(stream))
                            .getEventOptions(
                                    new SimpleConstrainedMap<>(
                                            UnitLookup.PLAIN_TEXT.getPersister())));
        } catch (IOException | ParseException e) {
            logger.error(e);
            return Optional.empty();
        }
    }

    @Override
    @Blocking
    public List<Template> getTemplates() throws FlightRecorderException {
        return getObjects().stream()
                .map(
                        t -> {
                            try {
                                return convertObject(t);
                            } catch (InvalidEventTemplateException e) {
                                logger.error(e);
                                return null;
                            }
                        })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Blocking
    public Optional<Document> getXml(String templateName, TemplateType unused)
            throws FlightRecorderException {
        try (var stream = getModel(templateName)) {
            Document doc =
                    Jsoup.parse(stream, StandardCharsets.UTF_8.name(), "", Parser.xmlParser());
            return Optional.of(doc);
        } catch (IOException e) {
            logger.error(e);
            return Optional.empty();
        }
    }

    @Blocking
    private List<S3Object> getObjects() {
        var builder = ListObjectsV2Request.builder().bucket(bucket);
        return storage.listObjectsV2(builder.build()).contents();
    }

    @Blocking
    private Template convertObject(S3Object object) throws InvalidEventTemplateException {
        var req = GetObjectTaggingRequest.builder().bucket(bucket).key(object.key()).build();
        var tagging = storage.getObjectTagging(req);
        var list = tagging.tagSet();
        if (!tagging.hasTagSet() || list.isEmpty()) {
            throw new InvalidEventTemplateException("No metadata found");
        }
        var decodedList = new ArrayList<Pair<String, String>>();
        list.forEach(
                t -> {
                    var encodedKey = t.key();
                    var decodedKey =
                            new String(base64Url.decode(encodedKey), StandardCharsets.UTF_8).trim();
<<<<<<< HEAD
                    var encodedValue = t.value();
                    var decodedValue =
                            new String(base64Url.decode(encodedValue), StandardCharsets.UTF_8)
                                    .trim();
                    decodedList.add(Pair.of(decodedKey, decodedValue));
                });
        var label =
                decodedList.stream()
                        .filter(t -> t.getKey().equals("label"))
                        .map(Pair::getValue)
                        .findFirst()
                        .orElseThrow();
        var description =
                decodedList.stream()
                        .filter(t -> t.getKey().equals("description"))
                        .map(Pair::getValue)
                        .findFirst()
                        .orElseThrow();
        var provider =
                decodedList.stream()
                        .filter(t -> t.getKey().equals("provider"))
                        .map(Pair::getValue)
                        .findFirst()
                        .orElseThrow();

        return new Template(label, description, provider, TemplateType.CUSTOM);
    }

    @Blocking
    private InputStream getModel(String name) {
        var req = GetObjectRequest.builder().bucket(bucket).key(name).build();
        return storage.getObject(req);
    }

    @Blocking
    private XMLModel parseXml(InputStream inputStream) throws IOException, ParseException {
        try (inputStream) {
            var model = EventConfiguration.createModel(inputStream);
=======
=======
    @Blocking
>>>>>>> 40f02da6 (update blocking annotations, make some methods private)
    public Optional<IConstrainedMap<EventOptionID>> getEvents(
            String templateName, TemplateType unused) throws FlightRecorderException {
        try (var stream = getModel(templateName)) {
            return Optional.of(
                    new EventConfiguration(parseXml(stream))
                            .getEventOptions(
                                    new SimpleConstrainedMap<>(
                                            UnitLookup.PLAIN_TEXT.getPersister())));
        } catch (IOException | ParseException e) {
            logger.error(e);
            return Optional.empty();
        }
    }

    @Override
    @Blocking
    public List<Template> getTemplates() throws FlightRecorderException {
<<<<<<< HEAD
        return convertObjects(getObjects());
    }

    @Override
    public Optional<Document> getXml(String templateName, TemplateType unused)
            throws FlightRecorderException {
        return getObject(templateName)
                .map(this::getContents)
                .map(
                        stream -> {
                            try (stream) {
                                Document doc =
                                        Jsoup.parse(
                                                stream,
                                                StandardCharsets.UTF_8.name(),
                                                "",
                                                Parser.xmlParser());
                                return doc;
                            } catch (IOException e) {
                                logger.error(e);
                                return null;
                            }
                        });
    }

    @Blocking
<<<<<<< HEAD
    public Template addTemplate(String templateText)
            throws InvalidXmlException, InvalidEventTemplateException, IOException {
        try {
            XMLModel model = EventConfiguration.createModel(templateText);
>>>>>>> f1bce2df (refactor, split out custom event templates service)
=======
    private Optional<S3Object> getObject(String name) {
        return getObjects().stream().filter(o -> o.key().equals(name)).findFirst();
    }

    @Blocking
    private List<S3Object> getObjects() {
        var builder = ListObjectsV2Request.builder().bucket(eventTemplatesBucket);
        return storage.listObjectsV2(builder.build()).contents();
    }

    private List<Template> convertObjects(List<S3Object> objects) {
        return objects.stream()
=======
        return getObjects().stream()
>>>>>>> e6b1f842 (refactor cleanup)
                .map(
                        t -> {
                            try {
                                return convertObject(t);
                            } catch (InvalidEventTemplateException e) {
                                logger.error(e);
                                return null;
                            }
                        })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Blocking
    public Optional<Document> getXml(String templateName, TemplateType unused)
            throws FlightRecorderException {
        try (var stream = getModel(templateName)) {
            Document doc =
                    Jsoup.parse(stream, StandardCharsets.UTF_8.name(), "", Parser.xmlParser());
            return Optional.of(doc);
        } catch (IOException e) {
            logger.error(e);
            return Optional.empty();
        }
    }

    @Blocking
    private List<S3Object> getObjects() {
        var builder = ListObjectsV2Request.builder().bucket(eventTemplatesBucket);
        return storage.listObjectsV2(builder.build()).contents();
    }

    @Blocking
    private Template convertObject(S3Object object) throws InvalidEventTemplateException {
        var req =
                GetObjectTaggingRequest.builder()
                        .bucket(eventTemplatesBucket)
                        .key(object.key())
                        .build();
        var tagging = storage.getObjectTagging(req);
        var list = tagging.tagSet();
        if (!tagging.hasTagSet() || list.isEmpty()) {
            throw new InvalidEventTemplateException("No metadata found");
        }
        var decodedList = new ArrayList<Pair<String, String>>();
        list.forEach(
                t -> {
                    var encodedKey = t.key();
                    var decodedKey =
                            new String(base64Url.decode(encodedKey), StandardCharsets.UTF_8);
=======
>>>>>>> a6276465 (trim strings)
                    var encodedValue = t.value();
                    var decodedValue =
                            new String(base64Url.decode(encodedValue), StandardCharsets.UTF_8)
                                    .trim();
                    decodedList.add(Pair.of(decodedKey, decodedValue));
                });
        var label =
                decodedList.stream()
                        .filter(t -> t.getKey().equals("label"))
                        .map(Pair::getValue)
                        .findFirst()
                        .orElseThrow();
        var description =
                decodedList.stream()
                        .filter(t -> t.getKey().equals("description"))
                        .map(Pair::getValue)
                        .findFirst()
                        .orElseThrow();
        var provider =
                decodedList.stream()
                        .filter(t -> t.getKey().equals("provider"))
                        .map(Pair::getValue)
                        .findFirst()
                        .orElseThrow();

        return new Template(label, description, provider, TemplateType.CUSTOM);
    }

    @Blocking
    private InputStream getModel(String name) {
        var req = GetObjectRequest.builder().bucket(eventTemplatesBucket).key(name).build();
        return storage.getObject(req);
    }

    @Blocking
    private XMLModel parseXml(InputStream inputStream) throws IOException, ParseException {
        try (inputStream) {
            var model = EventConfiguration.createModel(inputStream);
>>>>>>> af9dee6c (tmp)
            model.checkErrors();

            for (XMLValidationResult result : model.getResults()) {
                if (result.isError()) {
<<<<<<< HEAD
<<<<<<< HEAD
                    throw new IllegalArgumentException(
                            new InvalidEventTemplateException(result.getText()));
                }
            }
            return model;
        }
    }

    @Blocking
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 18935e67 (implement more suitable interface)
    @Override
    public Template addTemplate(InputStream stream)
            throws InvalidXmlException, InvalidEventTemplateException, IOException {
        try (stream) {
<<<<<<< HEAD
            XMLModel model = parseXml(stream);
=======
                    // throw new InvalidEventTemplateException(result.getText());
                    throw new IllegalArgumentException(result.getText());
=======
                    throw new IllegalArgumentException(
                            new InvalidEventTemplateException(result.getText()));
>>>>>>> c6897aea (cleanup)
                }
            }
>>>>>>> f1bce2df (refactor, split out custom event templates service)
=======
    public Template addTemplate(String templateText)
=======
    Template addTemplate(String templateText)
>>>>>>> a786557a (implement DELETE custom template)
            throws InvalidXmlException, InvalidEventTemplateException, IOException {
<<<<<<< HEAD
        try {
            XMLModel model =
                    parseXml(
                            new ByteArrayInputStream(
                                    templateText.getBytes(StandardCharsets.UTF_8)));
>>>>>>> af9dee6c (tmp)
=======
        try (var stream = new ByteArrayInputStream(templateText.getBytes(StandardCharsets.UTF_8))) {
=======
>>>>>>> 18935e67 (implement more suitable interface)
            XMLModel model = parseXml(stream);
>>>>>>> e6b1f842 (refactor cleanup)

            XMLTagInstance configuration = model.getRoot();
            XMLAttributeInstance labelAttr = null;
            for (XMLAttributeInstance attr : configuration.getAttributeInstances()) {
                if (attr.getAttribute().getName().equals("label")) {
                    labelAttr = attr;
                    break;
                }
            }

            if (labelAttr == null) {
<<<<<<< HEAD
<<<<<<< HEAD
                throw new IllegalArgumentException(
                        new InvalidEventTemplateException(
                                "Template has no configuration label attribute"));
            }

            String templateName = labelAttr.getExplicitValue().replaceAll("[\\W]+", "_");
=======
                // throw new InvalidEventTemplateException(
                //         "Template has no configuration label attribute");
                throw new IllegalArgumentException("Template has no configuration label attribute");
=======
                throw new IllegalArgumentException(
                        new InvalidEventTemplateException(
                                "Template has no configuration label attribute"));
>>>>>>> c6897aea (cleanup)
            }

            String templateName = labelAttr.getExplicitValue();
            templateName = templateName.replaceAll("[\\W]+", "_");
>>>>>>> f1bce2df (refactor, split out custom event templates service)

            XMLTagInstance root = model.getRoot();
            root.setValue(JFCGrammar.ATTRIBUTE_LABEL_MANDATORY, templateName);

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
            String description = getAttributeValue(root, "description");
            String provider = getAttributeValue(root, "provider");
            storage.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(templateName)
                            .contentType(ContentType.APPLICATION_XML.getMimeType())
                            .tagging(createTemplateTagging(templateName, description, provider))
                            .build(),
                    RequestBody.fromString(model.toString()));

            var template = new Template(templateName, description, provider, TemplateType.CUSTOM);
            bus.publish(
                    MessagingServer.class.getName(),
<<<<<<< HEAD
<<<<<<< HEAD
                    new Notification(EVENT_TEMPLATE_CREATED, Map.of("template", template)));
=======
                    new Notification(EVENT_TEMPLATE_CREATED, template));
>>>>>>> a1cad030 (publish notification on template upload)
=======
                    new Notification(EVENT_TEMPLATE_CREATED, Map.of("template", template)));
>>>>>>> a786557a (implement DELETE custom template)
            return template;
        } catch (IOException ioe) {
            // FIXME InvalidXmlException constructor should be made public in -core
            // throw new InvalidXmlException("Unable to parse XML stream", ioe);
            throw new IllegalArgumentException("Unable to parse XML stream", ioe);
        } catch (ParseException | IllegalArgumentException e) {
            throw new IllegalArgumentException(new InvalidEventTemplateException("Invalid XML", e));
        }
    }

    @Blocking
<<<<<<< HEAD
<<<<<<< HEAD
    @Override
    public void deleteTemplate(String templateName) {
        try {
            var template =
                    getTemplates().stream()
                            .filter(t -> t.getName().equals(templateName))
                            .findFirst()
                            .orElseThrow();
            var req = DeleteObjectRequest.builder().bucket(bucket).key(templateName).build();
            if (storage.deleteObject(req).sdkHttpResponse().isSuccessful()) {
                bus.publish(
                        MessagingServer.class.getName(),
                        new Notification(EVENT_TEMPLATE_DELETED, Map.of("template", template)));
            }
        } catch (FlightRecorderException e) {
            logger.error(e);
        }
<<<<<<< HEAD
=======
    void removeTemplate(String templateName) {
=======
    @Override
    public void deleteTemplate(String templateName) {
>>>>>>> 18935e67 (implement more suitable interface)
        var req =
                DeleteObjectRequest.builder()
                        .bucket(eventTemplatesBucket)
                        .key(templateName)
                        .build();
        storage.deleteObject(req);
        bus.publish(
                MessagingServer.class.getName(),
                new Notification(
                        EVENT_TEMPLATE_DELETED, Map.of("template", Map.of("name", templateName))));
>>>>>>> a786557a (implement DELETE custom template)
=======
>>>>>>> 8a65cbe9 (better pre-delete checks, emit full template description on notification)
    }

    private Tagging createTemplateTagging(
            String templateName, String description, String provider) {
        var map = Map.of("label", templateName, "description", description, "provider", provider);
        var tags = new ArrayList<Tag>();
        tags.addAll(
                map.entrySet().stream()
                        .map(
                                e ->
                                        Tag.builder()
                                                .key(
                                                        base64Url.encodeAsString(
                                                                e.getKey()
                                                                        .getBytes(
                                                                                StandardCharsets
                                                                                        .UTF_8)))
                                                .value(
                                                        base64Url.encodeAsString(
                                                                e.getValue()
                                                                        .getBytes(
                                                                                StandardCharsets
                                                                                        .UTF_8)))
                                                .build())
                        .toList());
        return Tagging.builder().tagSet(tags).build();
    }

=======
=======
            // TODO put the template description, provider, and other attributes in metadata so we
            // don't need to download and parse the whole XML just to display the templates list
>>>>>>> af9dee6c (tmp)
            String key = templateName;
=======
            String description = getAttributeValue(root, "description");
            String provider = getAttributeValue(root, "provider");
>>>>>>> 1208a8f3 (store attrs as metadata tags)
            storage.putObject(
                    PutObjectRequest.builder()
                            .bucket(eventTemplatesBucket)
                            .key(templateName)
                            .contentType(ContentType.APPLICATION_XML.getMimeType())
                            .tagging(createTemplateTagging(templateName, description, provider))
                            .build(),
                    RequestBody.fromString(model.toString()));

            return new Template(templateName, description, provider, TemplateType.CUSTOM);
        } catch (IOException ioe) {
            // FIXME InvalidXmlException constructor should be made public in -core
            // throw new InvalidXmlException("Unable to parse XML stream", ioe);
            throw new IllegalArgumentException("Unable to parse XML stream", ioe);
        } catch (ParseException | IllegalArgumentException e) {
            throw new IllegalArgumentException(new InvalidEventTemplateException("Invalid XML", e));
        }
    }

<<<<<<< HEAD
>>>>>>> f1bce2df (refactor, split out custom event templates service)
=======
    private Tagging createTemplateTagging(
            String templateName, String description, String provider) {
        var map = Map.of("label", templateName, "description", description, "provider", provider);
        var tags = new ArrayList<Tag>();
        tags.addAll(
                map.entrySet().stream()
                        .map(
                                e ->
                                        Tag.builder()
                                                .key(
                                                        base64Url.encodeAsString(
                                                                e.getKey()
                                                                        .getBytes(
                                                                                StandardCharsets
                                                                                        .UTF_8)))
                                                .value(
                                                        base64Url.encodeAsString(
                                                                e.getValue()
                                                                        .getBytes(
                                                                                StandardCharsets
                                                                                        .UTF_8)))
                                                .build())
                        .toList());
        return Tagging.builder().tagSet(tags).build();
    }

>>>>>>> 1208a8f3 (store attrs as metadata tags)
    protected String getAttributeValue(XMLTagInstance node, String valueKey) {
        return node.getAttributeInstances().stream()
                .filter(i -> Objects.equals(valueKey, i.getAttribute().getName()))
                .map(i -> i.getValue())
                .findFirst()
                .get();
    }
}
