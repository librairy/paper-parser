/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.paperparser.services;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import edu.upf.taln.dri.lib.model.Document;
import lombok.Setter;
import org.librairy.paperparser.data.AnnotatedPaper;
import org.librairy.paperparser.processor.GateProcessor;
import org.librairy.paperparser.processor.UpfProcessor;
import org.librairy.paperparser.utils.Serializations;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Part;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.executor.ParallelExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class PaperParser {

    private static final Logger LOG = LoggerFactory.getLogger(PaperParser.class);

    @Setter
    @Autowired
    GateProcessor gateProcessor;

    @Setter
    @Autowired
    UpfProcessor upfProcessor;

    @Autowired
    UDM udm;

    @Value("#{environment['LIBRAIRY_PAPERS_PARALLEL']?:${librairy.upf.parallel}}")
    Integer parallel;

    private ThreadPoolExecutor pool;

    @PostConstruct
    public void setup(){
        this.pool = new ThreadPoolExecutor(parallel, parallel, 1L, TimeUnit.MINUTES, new ArrayBlockingQueue(parallel, true), new
                ThreadPoolExecutor.CallerRunsPolicy());
    }

    private AnnotatedPaper process(String path){
        LOG.info("Processing file: " + path);
        String localPath = path.replace("http:","file:");

        Instant start = Instant.now();
        // Check if it was previously serialized
        if (new File(localPath + ".ser").exists()) {
            return Serializations.deserialize(AnnotatedPaper.class,localPath + ".ser");
        }

        // Process
        AnnotatedPaper annotatedPaper = new AnnotatedPaper();

        try {
            Document document = gateProcessor.process(path);

            annotatedPaper = upfProcessor.process(document);

            Serializations.serialize(annotatedPaper,localPath + ".ser");

        } catch (Exception e) {
            LOG.warn("Error parsing file: " + path, e);
        }

        Instant end = Instant.now();
        LOG.info("File processed in: " + ChronoUnit.MINUTES.between(start,end) + "min " + ChronoUnit.SECONDS
                .between(start,end) + "secs");

        return annotatedPaper;
    }

    public void handleParallel(Relation relation){
        pool.execute(() -> handle(relation));
    }

    public void handle(Relation relation)  {

        String documentUri = relation.getStartUri();
        String itemUri = relation.getEndUri();

        org.librairy.model.domain.resources.Document document = udm.read(Resource.Type.DOCUMENT).byUri(documentUri)
                .get().asDocument();
        String path = document.getRetrievedFrom();
        AnnotatedPaper paper = process(path);

        // Increase metainf of document
        document.setTitle(paper.getTitle());
        document.setDescription(paper.getSummary());
        if (Strings.isNullOrEmpty(document.getAuthoredBy())) document.setAuthoredBy(paper.getAuthors());
        if (Strings.isNullOrEmpty(document.getPublishedOn())) document.setPublishedOn(paper.getYear());
        if (Strings.isNullOrEmpty(document.getAuthoredOn())) document.setAuthoredOn(paper.getYear());
        udm.update(document);

        // create parts
        paper.getRhetoricalClasses().entrySet().forEach(entry -> {

            Part part = Resource.newPart(entry.getValue());
            part.setSense(entry.getKey());
            part.setTokens("");

            udm.save(part);
            udm.save(Relation.newDescribes(part.getUri(),itemUri));
            LOG.info("New ("+part.getSense()+") Part: " + part.getUri() + " created from Document: " + document.getUri());

        });

    }

}
