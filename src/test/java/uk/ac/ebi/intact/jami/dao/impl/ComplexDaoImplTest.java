package uk.ac.ebi.intact.jami.dao.impl;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.jami.IntactTestUtils;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/intact-jami-test-spring.xml"})
@Transactional(value = "jamiTransactionManager")
@TransactionConfiguration
@DirtiesContext
public class ComplexDaoImplTest {

    @PersistenceContext(unitName = "intact-jami")
    protected EntityManager entityManager;

    @Resource
    private IntactDao intactDao;

    private IntactComplex complex_1_1;
    private IntactComplex complex_1_2;
    private IntactComplex complex_1_3;
    private IntactComplex complex_2_1;

    @Before
    public void setUp() throws Exception {
        complex_1_1 = IntactTestUtils.createIntactComplex("CPX-1", "1");

        complex_1_2 = IntactTestUtils.createIntactComplex("CPX-1", "2");
        complex_1_2.addParticipant(IntactTestUtils.createIntactModelledParticipant2());

        complex_1_3 = IntactTestUtils.createIntactComplex("CPX-1", "3");
        complex_1_3.addParticipant(IntactTestUtils.createIntactModelledParticipant2());
        complex_1_3.addParticipant(IntactTestUtils.createIntactModelledParticipant3());

        complex_2_1 = IntactTestUtils.createIntactComplex("CPX-2", "1");
        complex_2_1.setOrganism(IntactTestUtils.createIntactOrganism2());

        intactDao.getComplexDao().persist(complex_1_1);
        intactDao.getComplexDao().persist(complex_1_2);
        intactDao.getComplexDao().persist(complex_1_3);
        intactDao.getComplexDao().persist(complex_2_1);
        this.entityManager.flush();

    }

    @After
    public void tearDown() throws Exception {
        intactDao.getComplexDao().delete(complex_1_1);
        intactDao.getComplexDao().delete(complex_1_2);
        intactDao.getComplexDao().delete(complex_1_3);
        intactDao.getComplexDao().delete(complex_2_1);
    }

    @Test
    @Transactional
    public void getLatestComplexVersionByComplexAc() {

        IntactComplex complex = intactDao.getComplexDao().getLatestComplexVersionByComplexAc("CPX-1");
        Assert.assertEquals("3", complex.getComplexVersion());

        complex = intactDao.getComplexDao().getLatestComplexVersionByComplexAc("CPX-2");
        Assert.assertEquals("1", complex.getComplexVersion());

    }

    @Test
    @Transactional
    public void getByComplexAcAndVersion() {
        Assert.assertNotNull(intactDao.getComplexDao().getByComplexAcAndVersion("CPX-1", "1"));
        Assert.assertNotNull(intactDao.getComplexDao().getByComplexAcAndVersion("CPX-1", "2"));
        Assert.assertNotNull(intactDao.getComplexDao().getByComplexAcAndVersion("CPX-1", "3"));
        Assert.assertNotNull(intactDao.getComplexDao().getByComplexAcAndVersion("CPX-2", "1"));
    }

    @Test
    public void getByComplexAc() {

        Collection<IntactComplex> complexes = intactDao.getComplexDao().getByComplexAc("CPX-1");
        Assert.assertEquals(3, complexes.size());
    }


}