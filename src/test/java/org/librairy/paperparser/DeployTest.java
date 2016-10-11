/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.paperparser;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by cbadenes on 14/01/16.
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
@TestPropertySource(properties = {
        "librairy.columndb.host = wiener.dia.fi.upm.es",
        "librairy.documentdb.host = wiener.dia.fi.upm.es",
        "librairy.graphdb.host = wiener.dia.fi.upm.es",
        "librairy.eventbus.host = wiener.dia.fi.upm.es",
        "librairy.uri = drinventor.eu" //librairy.org
})
public class DeployTest {

    private static final Logger LOG = LoggerFactory.getLogger(DeployTest.class);

    @Test
    public void run() throws InterruptedException {
        LOG.info("Sleepping...");
        Thread.sleep(10000000);
        LOG.info("Wake Up!");
    }

}
