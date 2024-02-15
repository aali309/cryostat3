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

import java.util.List;
import java.util.Optional;

import org.openjdk.jmc.common.unit.IConstrainedMap;
import org.openjdk.jmc.flightrecorder.configuration.events.EventOptionID;

import io.cryostat.core.FlightRecorderException;
import io.cryostat.core.templates.Template;
import io.cryostat.core.templates.TemplateService;
import io.cryostat.core.templates.TemplateType;
import io.cryostat.targets.Target;
import io.cryostat.targets.TargetConnectionManager;

<<<<<<< HEAD
import io.smallrye.common.annotation.Blocking;
=======
>>>>>>> f17134c2 (extract target templates service to separate class)
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jsoup.nodes.Document;

<<<<<<< HEAD
public class TargetTemplateService implements TemplateService {

    @ApplicationScoped
    public static class Factory {
        @Inject TargetConnectionManager connectionManager;

        public TargetTemplateService create(Target target) {
=======
class TargetTemplateService implements TemplateService {

    @ApplicationScoped
    static class Factory {
        @Inject TargetConnectionManager connectionManager;

        TargetTemplateService create(Target target) {
>>>>>>> f17134c2 (extract target templates service to separate class)
            return new TargetTemplateService(connectionManager, target);
        }
    }

    private final TargetConnectionManager connectionManager;
    private final Target target;

<<<<<<< HEAD
    private TargetTemplateService(TargetConnectionManager connectionManager, Target target) {
=======
    public TargetTemplateService(TargetConnectionManager connectionManager, Target target) {
>>>>>>> f17134c2 (extract target templates service to separate class)
        this.connectionManager = connectionManager;
        this.target = target;
    }

    @Override
<<<<<<< HEAD
    @Blocking
=======
>>>>>>> f17134c2 (extract target templates service to separate class)
    public List<Template> getTemplates() throws FlightRecorderException {
        return connectionManager.executeConnectedTask(
                target,
                connection ->
                        connection.getTemplateService().getTemplates().stream()
                                .filter(t -> t.getType().equals(TemplateType.TARGET))
                                .toList());
    }

    @Override
<<<<<<< HEAD
    @Blocking
=======
>>>>>>> f17134c2 (extract target templates service to separate class)
    public Optional<Document> getXml(String templateName, TemplateType unused)
            throws FlightRecorderException {
        return connectionManager.executeConnectedTask(
                target,
                conn -> conn.getTemplateService().getXml(templateName, TemplateType.TARGET));
    }

    @Override
<<<<<<< HEAD
    @Blocking
=======
>>>>>>> f17134c2 (extract target templates service to separate class)
    public Optional<IConstrainedMap<EventOptionID>> getEvents(
            String templateName, TemplateType unused) throws FlightRecorderException {
        return connectionManager.executeConnectedTask(
                target,
                conn -> conn.getTemplateService().getEvents(templateName, TemplateType.TARGET));
    }
}
