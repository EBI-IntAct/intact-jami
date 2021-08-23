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
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.InteractorPool;
import psidev.psi.mi.jami.model.ModelledParticipant;
import psidev.psi.mi.jami.model.Stoichiometry;
import psidev.psi.mi.jami.model.impl.*;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.IntactTestUtils;
import uk.ac.ebi.intact.jami.context.DefaultSynchronizerContext;
import uk.ac.ebi.intact.jami.model.extension.*;
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
    public void postFilterComplexesListenerTest() throws PersisterException, FinderException, SynchronizerException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
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
        Assert.assertEquals(2, complexSynchronizer.postFilterComplexes(newObject1, persistableIntactComplexes).size());

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
        Assert.assertEquals(0, complexSynchronizer.postFilterComplexes(newObject2, persistableIntactComplexes).size());

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

    @Transactional
    @Test
    @DirtiesContext
    public void find_duplicate_complex_1() throws PersisterException, FinderException, SynchronizerException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ComplexSynchronizer complexSynchronizer = (ComplexSynchronizer) this.synchronizer;

        IntactModelledParticipant intactModelledParticipant1 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant1.setStoichiometry(new IntactStoichiometry(2));

        intactModelledParticipant1.setInteractor(new IntactProtein("test protein",
                IntactTestUtils.createUniprotXref(InteractorXref.class,"UNIPROT_ID_1")));

        List<IntactModelledParticipant> intactModelledParticipantList1 = new ArrayList<>();
        intactModelledParticipantList1.add(intactModelledParticipant1);
        IntactComplex objectToTest1 = createComplexWithParticipants(intactModelledParticipantList1);
        objectToTest1.setShortName("persistable complex");
        //objectToTest1.setAc("EBI-1");

        this.synchronizer.persist(objectToTest1);

        //new object
        IntactModelledParticipant intactModelledParticipant2 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant2.setStoichiometry(new IntactStoichiometry(2));
        intactModelledParticipant2.setInteractor(new DefaultProtein("test protein",
                XrefUtils.createUniprotIdentity("UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList2 = new ArrayList<>();
        intactModelledParticipantList2.add(intactModelledParticipant2);
        IntactComplex newObject1 = createComplexWithParticipants(intactModelledParticipantList2);

        // test cache if any
        Assert.assertNotNull(complexSynchronizer.findAllMatchingComplexAcs(newObject1));

        //clear cache
        this.synchronizer.clearCache();
        this.entityManager.flush();

        Assert.assertEquals(1,complexSynchronizer.findComplexesByProteins(newObject1).size());
        Assert.assertEquals(1,complexSynchronizer.findAllMatchingComplexAcs(newObject1).size());
    }

    @Transactional
    @Test
    @DirtiesContext
    public void find_duplicate_complex_2()throws PersisterException, FinderException, SynchronizerException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
        ComplexSynchronizer complexSynchronizer = (ComplexSynchronizer) this.synchronizer;

        // creation of persistable complex1
        IntactModelledParticipant intactModelledParticipant1 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant1.setInteractor(new IntactProtein("test protein1",
                IntactTestUtils.createUniprotXref(InteractorXref.class,"UNIPROT_ID_1")));
        List<IntactModelledParticipant> intactModelledParticipantList1 = new ArrayList<>();
        intactModelledParticipantList1.add(intactModelledParticipant1);
        IntactComplex persistableComplex1 = createComplexWithParticipants(intactModelledParticipantList1);
        persistableComplex1.setShortName("persistable complex1");

        // creation of common protein
        IntactProtein commonProtein=new IntactProtein("test protein3",
                IntactTestUtils.createUniprotXref(InteractorXref.class,"P12345"));

        // creation of persistable complex2
        IntactModelledParticipant intactModelledParticipant2 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant2.setInteractor(new IntactProtein("test protein2",
                IntactTestUtils.createUniprotXref(InteractorXref.class,"P12346")));
        intactModelledParticipant2.setStoichiometry(0);// we do not want to compare stoichiometry in this test

        IntactModelledParticipant intactModelledParticipant3 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant3.setInteractor(commonProtein);
        intactModelledParticipant3.setStoichiometry(0);

        IntactModelledParticipant intactModelledParticipant4 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant4.setInteractor(new IntactNucleicAcid("test"));
        intactModelledParticipant4.setStoichiometry(0);

        //creating complex as an interactor
        IntactModelledParticipant complexParticipant1 = IntactTestUtils.createIntactModelledParticipant();
        complexParticipant1.setInteractor(commonProtein);

        IntactModelledParticipant complexParticipant2 = IntactTestUtils.createIntactModelledParticipant();
        complexParticipant2.setInteractor(new IntactProtein("test protein4",
                IntactTestUtils.createUniprotXref(InteractorXref.class,"P12347")));

        List<IntactModelledParticipant> complexParticipants = new ArrayList<>();
        complexParticipants.add(complexParticipant1);
        complexParticipants.add(complexParticipant2);
        IntactComplex complexAsAnInteractor1 = createComplexWithParticipants(complexParticipants);
        complexAsAnInteractor1.setShortName("complex as an interactor");
        IntactModelledParticipant intactModelledParticipant5 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant5.setInteractor(complexAsAnInteractor1);
        intactModelledParticipant5.setStoichiometry(0);

        //creating pool as an interactor
        IntactInteractorPool pool=IntactTestUtils.createEmptyIntactInteractorPool();
        pool.setShortName("pool");
        pool.add(commonProtein);

        IntactModelledParticipant intactModelledParticipant6 = IntactTestUtils.createIntactModelledParticipant();
        intactModelledParticipant6.setInteractor(pool);
        intactModelledParticipant6.setStoichiometry(0);

        List<IntactModelledParticipant> intactModelledParticipantList2 = new ArrayList<>();
        intactModelledParticipantList2.add(intactModelledParticipant2);
        intactModelledParticipantList2.add(intactModelledParticipant3);
        intactModelledParticipantList2.add(intactModelledParticipant4);
        intactModelledParticipantList2.add(intactModelledParticipant5);
        intactModelledParticipantList2.add(intactModelledParticipant6);

        IntactComplex persistableComplex2 = createComplexWithParticipants(intactModelledParticipantList2);
        persistableComplex2.setShortName("persistable complex2");

        complexSynchronizer.persist(persistableComplex1);
        complexSynchronizer.persist(persistableComplex2);

         //creation of new complex object

        Complex newComplex = new DefaultComplex("complex_interactor", new DefaultCvTerm("protein complex"));
        newComplex.setInteractionType(new DefaultCvTerm("phosphorylation"));
        newComplex.addParticipant(new DefaultModelledParticipant(new DefaultProtein("test1 protein",
                XrefUtils.createUniprotIdentity("P12345")),new DefaultStoichiometry(0)));
        newComplex.addParticipant(new DefaultModelledParticipant(new DefaultProtein("test2 protein",
                XrefUtils.createUniprotIdentity("P12347")),new DefaultStoichiometry(0)));
        newComplex.addParticipant(new DefaultModelledParticipant(new DefaultProtein("test1 protein",
                XrefUtils.createUniprotIdentity("P12346")),new DefaultStoichiometry(0)));

        this.synchronizer.clearCache();
        this.entityManager.flush();

        Collection<IntactComplex> complexCollection=complexSynchronizer.findComplexesByProteins(newComplex);
        Assert.assertEquals(1,complexCollection.size());
        Assert.assertEquals("persistable complex2",complexCollection.iterator().next().getShortName());
        Collection<String> duplicateAcs=complexSynchronizer.findAllMatchingComplexAcs(newComplex);
        Assert.assertEquals(1,duplicateAcs.size());
        Assert.assertEquals(persistableComplex2.getAc(),duplicateAcs.iterator().next());

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
