/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.jami.sequence;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Unit tester for SequenceManager
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/intact-jami-test-spring.xml"})
@Transactional(value = "jamiTransactionManager")
@Rollback
@DirtiesContext
public class SequenceManagerTest{

    @Resource(name = "jamiSequenceManager")
    private SequenceManager seqManager;

    @Test
    public void createSequenceIfNotExists() throws Exception {

        Assert.assertFalse(seqManager.sequenceExists("public.lala_seq"));

        seqManager.createSequenceIfNotExists("public.lala_seq");

        Assert.assertTrue(seqManager.sequenceExists("public.lala_seq"));
    }

    @Test
    public void getNextValueForSequence() throws Exception {

//        beginTransaction();
        seqManager.createSequenceIfNotExists("public.test2_seq", 5);
//        commitTransaction();

        Assert.assertEquals(5L, seqManager.getNextValueForSequence("public.test2_seq").longValue());
        Assert.assertEquals(6L, seqManager.getNextValueForSequence("public.test2_seq").longValue());
        Assert.assertEquals(7L, seqManager.getNextValueForSequence("public.test2_seq").longValue());
        Assert.assertEquals(8L, seqManager.getNextValueForSequence("public.test2_seq").longValue());
        Assert.assertEquals(9L, seqManager.getNextValueForSequence("public.test2_seq").longValue());
        Assert.assertEquals(10L, seqManager.getNextValueForSequence("public.test2_seq").longValue());


    }


}
