/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.paperparser.processor;

import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.loader.PDFloaderImpl;
import edu.upf.taln.dri.lib.model.Document;
import lombok.Setter;
import org.librairy.paperparser.data.AnnotatedPaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created on 22/04/16:
 *
 * @author cbadenes
 */
@Component
public class GateProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(GateProcessor.class);

    @Setter
    @Value("${librairy.upf.miner.config}")
    File driConfigPath;

    @Setter
    @Value("${LIBRAIRY_UPF_PROXY:true}")
    Boolean proxyEnabled;

    private LoadingCache<String, AnnotatedPaper> cache;

    @PostConstruct
    public void setup() throws DRIexception {

        LOG.info("Initializing UPF Text Mining Framework from: " + driConfigPath.getAbsolutePath() + " ..");

        // Enable the PDFX proxy service
        //PDFloaderImpl.PDFXproxyEnabled = true;
        PDFloaderImpl.PDFXproxyEnabled = proxyEnabled;

        // Set property file path
        Factory.setDRIPropertyFilePath(driConfigPath.getAbsolutePath());

        // Enable bibliography entry parsing
        Factory.setEnableBibEntryParsing(false);

        // Initialize
        Factory.initFramework();

        LOG.info("UPF Text Mining Framework initialized successfully");
    }



    public Document process(String path){

        LOG.info("Trying to compose a UPF-Gate file from: " + path);
        Instant start           = Instant.now();
        String extension        = Files.getFileExtension(path).toLowerCase();
        Document document       = null;
        try {
            switch (extension) {
                case "pdf":
                    LOG.info("parsing document as PDF document: " + path);
                    if (path.startsWith("http:")){
                        document = Factory.getPDFloader().parsePDF(new URL(path));
                    }else if (path.startsWith("file:") || path.startsWith("/")){
                        document = Factory.getPDFloader().parsePDF(new File(path));
                    }else{
                        document = Factory.getPDFloader().parsePDF(path);
                    }
                    break;
                case "xml":
                case "htm":
                case "html":
                    LOG.info("parsing document as structured document: " + path);
                    document = Factory.createNewDocument(path);
                    break;
                default:
                    LOG.info("parsing document as plain text document: " + path);
                    document = Factory.getPlainTextLoader().parsePlainText(new File(path));
                    break;
            }
            Instant end = Instant.now();

            if (document == null) throw new IOException("Text Mining library error");

            LOG.info("Resource '"+path+ "' composed successfully as UPF-Gate file in: " + ChronoUnit
                    .MINUTES.between(start,end) + "min " + ChronoUnit.SECONDS
                    .between(start,end) + "secs");

            return document;

        } catch (Exception e) {
            throw new RuntimeException("Error processing file by Gate: " + path, e);
        }


    }

}
