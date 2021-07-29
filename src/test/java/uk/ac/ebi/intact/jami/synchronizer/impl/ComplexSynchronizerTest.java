package uk.ac.ebi.intact.jami.synchronizer.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.listener.comparator.event.ComplexComparisonEvent;
import psidev.psi.mi.jami.listener.comparator.impl.ComplexComparatorListenerImpl;
import psidev.psi.mi.jami.listener.comparator.observer.ComplexComparatorObserver;
import psidev.psi.mi.jami.model.impl.DefaultProtein;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.IntactTestUtils;
import uk.ac.ebi.intact.jami.context.DefaultSynchronizerContext;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;
import uk.ac.ebi.intact.jami.model.extension.IntactStoichiometry;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ComplexSynchronizerTest extends InteractorSynchronizerTemplateTest {

    private int complexAcGenerator = 0;

    @Before
    public void init() {
        this.context = new DefaultSynchronizerContext(this.entityManager);
        initSynchronizer();

    }

    @Override
    protected void initSynchronizer() {
        this.synchronizer = new ComplexSynchronizer(this.context);
    }

    @Test
    public void postFilterListenerTest() throws PersisterException, FinderException, SynchronizerException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ComplexSynchronizer complexSynchronizer = (ComplexSynchronizer) this.synchronizer;

        IntactModelledParticipant intactModelledParticipant1 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant1.setStoichiometry(new IntactStoichiometry(2));
        intactModelledParticipant1.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList1 = new ArrayList<>();
        intactModelledParticipantList1.add(intactModelledParticipant1);
        IntactComplex objectToTest = createComplexWithParticipants(intactModelledParticipantList1);

        List<IntactComplex> persistableIntactComplexes = new ArrayList();
        persistableIntactComplexes.add(objectToTest);

        // with same stoichiometry
        IntactModelledParticipant intactModelledParticipant2 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant2.setStoichiometry(new IntactStoichiometry(2));
        intactModelledParticipant2.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList2 = new ArrayList<>();
        intactModelledParticipantList2.add(intactModelledParticipant2);
        IntactComplex newObject1 = createComplexWithParticipants(intactModelledParticipantList2);

        ComplexComparatorListenerImpl complexComparatorListener1 = new ComplexComparatorListenerImpl();
        complexSynchronizer.setComplexComparatorListener(complexComparatorListener1);

        Assert.assertNotNull(complexSynchronizer.postFilter(newObject1, persistableIntactComplexes));
        Assert.assertEquals(0, complexComparatorListener1.getComplexComparatorObservations().size());

        // with different stoichiometry
        IntactModelledParticipant intactModelledParticipant3 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant3.setStoichiometry(new IntactStoichiometry(3));
        intactModelledParticipant3.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList3 = new ArrayList<>();
        intactModelledParticipantList3.add(intactModelledParticipant3);
        IntactComplex newObject2 = createComplexWithParticipants(intactModelledParticipantList3);

        ComplexComparatorListenerImpl complexComparatorListener2 = new ComplexComparatorListenerImpl();
        complexSynchronizer.setComplexComparatorListener(complexComparatorListener2);

        // post filter test
        Assert.assertNull(complexSynchronizer.postFilter(newObject2, persistableIntactComplexes));

        // observation
        List<ComplexComparatorObserver> complexComparatorObserverations = complexComparatorListener2.getComplexComparatorObservations();
        Assert.assertEquals(1, complexComparatorObserverations.size());
        Assert.assertEquals(1, complexComparatorObserverations.get(0).getDifferentObservations().size());
        complexComparatorObserverations.get(0);
        Assert.assertEquals(ComplexComparisonEvent.EventType.ONLY_STOICHIOMETRY_DIFFERENT,
                complexComparatorObserverations.get(0).getDifferentObservations().iterator().next().getEventType());
        Assert.assertEquals(newObject2.getComplexAc(), complexComparatorObserverations.get(0).getComplex1().getComplexAc());
        Assert.assertEquals(objectToTest.getComplexAc(), complexComparatorObserverations.get(0).getComplex2().getComplexAc());

        Assert.assertNull(complexSynchronizer.postFilter(newObject2, persistableIntactComplexes));
    }

    @Test
    public void postFilterAllListenerTest() throws PersisterException, FinderException, SynchronizerException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ComplexSynchronizer complexSynchronizer = (ComplexSynchronizer) this.synchronizer;

        IntactModelledParticipant intactModelledParticipant1 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant1.setStoichiometry(new IntactStoichiometry(2));
        intactModelledParticipant1.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList1 = new ArrayList<>();
        intactModelledParticipantList1.add(intactModelledParticipant1);
        IntactComplex objectToTest1 = createComplexWithParticipants(intactModelledParticipantList1);

        IntactModelledParticipant intactModelledParticipant4 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant4.setStoichiometry(new IntactStoichiometry(2));
        intactModelledParticipant4.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList4 = new ArrayList<>();
        intactModelledParticipantList4.add(intactModelledParticipant4);
        IntactComplex objectToTest2 = createComplexWithParticipants(intactModelledParticipantList4);

        List<IntactComplex> persistableIntactComplexes = new ArrayList();
        persistableIntactComplexes.add(objectToTest1);
        persistableIntactComplexes.add(objectToTest2);

        // with same stoichiometry
        IntactModelledParticipant intactModelledParticipant2 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant2.setStoichiometry(new IntactStoichiometry(2));
        intactModelledParticipant2.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList2 = new ArrayList<>();
        intactModelledParticipantList2.add(intactModelledParticipant2);
        IntactComplex newObject1 = createComplexWithParticipants(intactModelledParticipantList2);

        ComplexComparatorListenerImpl complexComparatorListener1 = new ComplexComparatorListenerImpl();
        complexSynchronizer.setComplexComparatorListener(complexComparatorListener1);

        // post filter test
        Assert.assertEquals(2, complexSynchronizer.postFilterAll(newObject1, persistableIntactComplexes).size());

        Assert.assertEquals(0, complexComparatorListener1.getComplexComparatorObservations().size());

        // with different stoichiometry
        IntactModelledParticipant intactModelledParticipant3 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant3.setStoichiometry(new IntactStoichiometry(3));
        intactModelledParticipant3.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList3 = new ArrayList<>();
        intactModelledParticipantList3.add(intactModelledParticipant3);
        IntactComplex newObject2 = createComplexWithParticipants(intactModelledParticipantList3);


        ComplexComparatorListenerImpl complexComparatorListener2 = new ComplexComparatorListenerImpl();
        complexSynchronizer.setComplexComparatorListener(complexComparatorListener2);

        // post filter test
        Assert.assertEquals(0, complexSynchronizer.postFilterAll(newObject2, persistableIntactComplexes).size());

        List<ComplexComparatorObserver> complexComparatorObserverations = complexComparatorListener2.getComplexComparatorObservations();
        Assert.assertEquals(2, complexComparatorObserverations.size());

        // comparator listener observation 1
        ComplexComparatorObserver complexComparatorObserver1 = complexComparatorObserverations.get(0);
        Assert.assertEquals(1, complexComparatorObserver1.getDifferentObservations().size());
        Assert.assertEquals(ComplexComparisonEvent.EventType.ONLY_STOICHIOMETRY_DIFFERENT,
                complexComparatorObserver1.getDifferentObservations().iterator().next().getEventType());
        Assert.assertEquals(newObject2.getComplexAc(), complexComparatorObserver1.getComplex1().getComplexAc());
        Assert.assertEquals(objectToTest1.getComplexAc(), complexComparatorObserver1.getComplex2().getComplexAc());

        // comparator listener observation 2
        ComplexComparatorObserver complexComparatorObserver2 = complexComparatorObserverations.get(1);
        Assert.assertEquals(1, complexComparatorObserver2.getDifferentObservations().size());
        Assert.assertEquals(ComplexComparisonEvent.EventType.ONLY_STOICHIOMETRY_DIFFERENT,
                complexComparatorObserver2.getDifferentObservations().iterator().next().getEventType());
        Assert.assertEquals(newObject2.getComplexAc(), complexComparatorObserver2.getComplex1().getComplexAc());
        Assert.assertEquals(objectToTest2.getComplexAc(), complexComparatorObserver2.getComplex2().getComplexAc());

        Assert.assertEquals(0, complexSynchronizer.postFilterAll(newObject2, persistableIntactComplexes).size());
    }

    @Test
    public void postFilterAllAcsListenerTest() throws PersisterException, FinderException, SynchronizerException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ComplexSynchronizer complexSynchronizer = (ComplexSynchronizer) this.synchronizer;

        IntactModelledParticipant intactModelledParticipant1 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant1.setStoichiometry(new IntactStoichiometry(2));
        intactModelledParticipant1.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList1 = new ArrayList<>();
        intactModelledParticipantList1.add(intactModelledParticipant1);
        IntactComplex objectToTest1 = createComplexWithParticipants(intactModelledParticipantList1);
        objectToTest1.setAc("EBI-1");

        IntactModelledParticipant intactModelledParticipant4 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant4.setStoichiometry(new IntactStoichiometry(2));
        intactModelledParticipant4.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList4 = new ArrayList<>();
        intactModelledParticipantList4.add(intactModelledParticipant4);
        IntactComplex objectToTest2 = createComplexWithParticipants(intactModelledParticipantList4);
        objectToTest2.setAc("EBI-2");
        List<IntactComplex> persistableIntactComplexes = new ArrayList();
        persistableIntactComplexes.add(objectToTest1);
        persistableIntactComplexes.add(objectToTest2);

        // with same stoichiometry
        IntactModelledParticipant intactModelledParticipant2 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant2.setStoichiometry(new IntactStoichiometry(2));
        intactModelledParticipant2.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList2 = new ArrayList<>();
        intactModelledParticipantList2.add(intactModelledParticipant2);
        IntactComplex newObject1 = createComplexWithParticipants(intactModelledParticipantList2);

        ComplexComparatorListenerImpl complexComparatorListener1 = new ComplexComparatorListenerImpl();
        complexSynchronizer.setComplexComparatorListener(complexComparatorListener1);

        // post filter test
        Assert.assertEquals(2, complexSynchronizer.postFilterAllAcs(newObject1, persistableIntactComplexes).size());

        Assert.assertEquals(0, complexComparatorListener1.getComplexComparatorObservations().size());

        // with different stoichiometry
        IntactModelledParticipant intactModelledParticipant3 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant3.setStoichiometry(new IntactStoichiometry(3));
        intactModelledParticipant3.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList3 = new ArrayList<>();
        intactModelledParticipantList3.add(intactModelledParticipant3);
        IntactComplex newObject2 = createComplexWithParticipants(intactModelledParticipantList3);


        ComplexComparatorListenerImpl complexComparatorListener2 = new ComplexComparatorListenerImpl();
        complexSynchronizer.setComplexComparatorListener(complexComparatorListener2);

        // post filter test
        Assert.assertEquals(0, complexSynchronizer.postFilterAllAcs(newObject2, persistableIntactComplexes).size());

        List<ComplexComparatorObserver> complexComparatorObserverations = complexComparatorListener2.getComplexComparatorObservations();
        Assert.assertEquals(2, complexComparatorObserverations.size());

        // comparator listener observation 1
        ComplexComparatorObserver complexComparatorObserver1 = complexComparatorObserverations.get(0);
        Assert.assertEquals(1, complexComparatorObserver1.getDifferentObservations().size());
        Assert.assertEquals(ComplexComparisonEvent.EventType.ONLY_STOICHIOMETRY_DIFFERENT,
                complexComparatorObserver1.getDifferentObservations().iterator().next().getEventType());
        Assert.assertEquals(newObject2.getComplexAc(), complexComparatorObserver1.getComplex1().getComplexAc());
        Assert.assertEquals(objectToTest1.getComplexAc(), complexComparatorObserver1.getComplex2().getComplexAc());

        // comparator listener observation 2
        ComplexComparatorObserver complexComparatorObserver2 = complexComparatorObserverations.get(1);
        Assert.assertEquals(1, complexComparatorObserver2.getDifferentObservations().size());
        Assert.assertEquals(ComplexComparisonEvent.EventType.ONLY_STOICHIOMETRY_DIFFERENT,
                complexComparatorObserver2.getDifferentObservations().iterator().next().getEventType());
        Assert.assertEquals(newObject2.getComplexAc(), complexComparatorObserver2.getComplex1().getComplexAc());
        Assert.assertEquals(objectToTest2.getComplexAc(), complexComparatorObserver2.getComplex2().getComplexAc());

        Assert.assertEquals(0, complexSynchronizer.postFilterAllAcs(newObject2, persistableIntactComplexes).size());
    }

    @Override
    protected IntactInteractor createDefaultObject() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return IntactTestUtils.createIntactComplex("COMPLEX-AC-" + (++complexAcGenerator), "1");
    }

    protected IntactComplex createComplexWithParticipants(Collection<IntactModelledParticipant> intactModelledParticipants) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return IntactTestUtils.createIntactComplexWithParticipants("COMPLEX-AC-" + (++complexAcGenerator), "1", intactModelledParticipants);
    }

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_persist_all() throws PersisterException, FinderException, SynchronizerException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_deleted() throws PersisterException, FinderException, SynchronizerException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_find() throws PersisterException, FinderException, SynchronizerException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    /*Below methods can be implemented if needed in future */

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_synchronize_properties() throws PersisterException, FinderException, SynchronizerException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_synchronize_not_persist() throws PersisterException, FinderException, SynchronizerException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_synchronize_persist() throws PersisterException, FinderException, SynchronizerException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_synchronize_merge() throws PersisterException, FinderException, SynchronizerException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_synchronize_merge2() throws PersisterException, FinderException, SynchronizerException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_jami() throws PersisterException, FinderException, SynchronizerException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }

    @Transactional
    @Test
    @DirtiesContext
    @Override
    @Ignore
    public void test_commonName_synch() throws PersisterException, FinderException, SynchronizerException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

    }

}
