/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.paperparser.eventbus;

import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Resource;
import org.librairy.paperparser.services.PaperParser;
import org.librairy.model.Event;
import org.librairy.model.modules.BindingKey;
import org.librairy.model.modules.EventBus;
import org.librairy.model.modules.EventBusSubscriber;
import org.librairy.model.modules.RoutingKey;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class NewBundleEventHandler implements EventBusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(NewBundleEventHandler.class);

    @Autowired
    protected EventBus eventBus;

    @Autowired
    PaperParser paperParser;

    @PostConstruct
    public void init(){
        BindingKey bindingKey = BindingKey.of(RoutingKey.of(Relation.Type.BUNDLES, Relation.State.CREATED),
                "paper-parser-new-bundles");
        LOG.info("Trying to register as subscriber of '" + bindingKey + "' events ..");
        eventBus.subscribe(this,bindingKey );
        LOG.info("registered successfully");
    }

    @Override
    public void handle(Event event) {
        LOG.debug("Trying to parser document: " + event);
        try{
            paperParser.handleParallel(event.to(Relation.class));

        } catch (Exception e){
            // TODO Notify to event-bus when source has not been added
            LOG.error("Error trying to retrieve parts from document: " + event, e);
        }
    }
}
