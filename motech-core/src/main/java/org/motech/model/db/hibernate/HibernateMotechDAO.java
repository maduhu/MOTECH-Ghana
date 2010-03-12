package org.motech.model.db.hibernate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.motech.model.Blackout;
import org.motech.model.ExpectedEncounter;
import org.motech.model.ExpectedObs;
import org.motech.model.GeneralPatientEncounter;
import org.motech.model.Log;
import org.motech.model.Message;
import org.motech.model.MessageAttribute;
import org.motech.model.MessageDefinition;
import org.motech.model.MessageProgramEnrollment;
import org.motech.model.MessageStatus;
import org.motech.model.ScheduledMessage;
import org.motech.model.Service;
import org.motech.model.ServiceStatus;
import org.motech.model.TroubledPhone;
import org.motech.model.db.MotechDAO;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;

/**
 * An implementation of the motech data access object interface, implemented
 * using the hibernate object relational mapping library.
 */
public class HibernateMotechDAO implements MotechDAO {

	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Log saveLog(Log log) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(log);
		return log;
	}

	@SuppressWarnings("unchecked")
	public List<Log> getLogs() {
		Session session = sessionFactory.getCurrentSession();
		return (List<Log>) session.createCriteria(Log.class).list();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getUsersByPersonAttribute(
			Integer personAttributeTypeId, String personAttributeValue) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Integer>) session
				.createSQLQuery(
						"select u.user_id from person p inner join users u on u.user_id = p.person_id "
								+ "inner join person_attribute a on p.person_id = a.person_id "
								+ "where a.voided = false and p.voided = false and u.voided = false "
								+ "and a.person_attribute_type_id = :typeId and a.value = :value group by u.user_id")
				.setInteger("typeId", personAttributeTypeId).setString("value",
						personAttributeValue).list();
	}

	public ScheduledMessage saveScheduledMessage(
			ScheduledMessage scheduledMessage) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(scheduledMessage);
		return scheduledMessage;
	}

	public Message saveMessage(Message message) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(message);
		return message;
	}

	public MessageDefinition saveMessageDefinition(
			MessageDefinition messageDefinition) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(messageDefinition);
		return messageDefinition;
	}

	public MessageAttribute saveMessageAttribute(
			MessageAttribute messageAttribute) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(messageAttribute);
		return messageAttribute;
	}

	@SuppressWarnings("unchecked")
	public List<ScheduledMessage> getScheduledMessages() {
		Session session = sessionFactory.getCurrentSession();
		return (List<ScheduledMessage>) session.createCriteria(
				ScheduledMessage.class).list();
	}

	@SuppressWarnings("unchecked")
	public List<ScheduledMessage> getScheduledMessages(Date startDate,
			Date endDate) {
		Session session = sessionFactory.getCurrentSession();
		return (List<ScheduledMessage>) session.createCriteria(
				ScheduledMessage.class).add(
				Restrictions.between("scheduledFor", startDate, endDate))
				.list();
	}

	@SuppressWarnings("unchecked")
	public List<ScheduledMessage> getScheduledMessages(Integer recipientId,
			MessageDefinition definition, MessageProgramEnrollment enrollment,
			Date messageDate) {
		Session session = sessionFactory.getCurrentSession();
		return (List<ScheduledMessage>) session.createCriteria(
				ScheduledMessage.class).add(
				Restrictions.eq("recipientId", recipientId)).add(
				Restrictions.eq("message", definition)).add(
				Restrictions.eq("enrollment", enrollment)).add(
				Restrictions.eq("scheduledFor", messageDate)).list();
	}

	@SuppressWarnings("unchecked")
	public List<Message> getMessages() {
		Session session = sessionFactory.getCurrentSession();
		return (List<Message>) session.createCriteria(Message.class).list();
	}

	@SuppressWarnings("unchecked")
	public List<Message> getMessages(Date startDate, Date endDate,
			MessageStatus status) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Message>) session.createCriteria(Message.class).add(
				Restrictions.between("attemptDate", startDate, endDate)).add(
				Restrictions.eq("attemptStatus", status)).list();
	}

	@SuppressWarnings("unchecked")
	public List<Message> getMessages(MessageProgramEnrollment enrollment,
			MessageStatus status) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Message>) session.createCriteria(Message.class).add(
				Restrictions.eq("attemptStatus", status)).createCriteria(
				"schedule").add(Restrictions.eq("enrollment", enrollment))
				.list();
	}

	@SuppressWarnings("unchecked")
	public List<Message> getMessages(Integer recipientId,
			MessageProgramEnrollment enrollment, MessageDefinition definition,
			Date messageDate, MessageStatus status) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Message>) session.createCriteria(Message.class).add(
				Restrictions.eq("attemptStatus", status)).createCriteria(
				"schedule").add(Restrictions.eq("recipientId", recipientId))
				.add(Restrictions.eq("enrollment", enrollment)).add(
						Restrictions.or(Restrictions.ne("message", definition),
								Restrictions.ne("scheduledFor", messageDate)))
				.list();
	}

	public Message getMessage(String publicId) {
		Session session = sessionFactory.getCurrentSession();
		return (Message) session.createCriteria(Message.class).add(
				Restrictions.eq("publicId", publicId)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<Message> getMessages(ScheduledMessage scheduledMessage) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Message>) session.createCriteria(Message.class).add(
				Restrictions.eq("schedule", scheduledMessage)).list();
	}

	@SuppressWarnings("unchecked")
	public List<MessageDefinition> getMessageDefinitions() {
		Session session = sessionFactory.getCurrentSession();
		return (List<MessageDefinition>) session.createCriteria(
				MessageDefinition.class).list();
	}

	public MessageDefinition getMessageDefinition(String messageKey) {
		Session session = sessionFactory.getCurrentSession();
		return (MessageDefinition) session.createCriteria(
				MessageDefinition.class).add(
				Restrictions.eq("messageKey", messageKey)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<MessageAttribute> getMessageAttributes() {
		Session session = sessionFactory.getCurrentSession();
		return (List<MessageAttribute>) session.createCriteria(
				MessageAttribute.class).list();
	}

	public Blackout getBlackoutSettings() {
		Blackout settings = (Blackout) sessionFactory.getCurrentSession()
				.createCriteria(Blackout.class).uniqueResult();
		return settings;
	}

	public void setBlackoutSettings(Blackout settings) {
		sessionFactory.getCurrentSession().saveOrUpdate(settings);
	}

	public void saveTroubledPhone(TroubledPhone phone) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(phone);
	}

	public TroubledPhone getTroubledPhone(Long id) {
		Session session = sessionFactory.getCurrentSession();
		TroubledPhone tp = (TroubledPhone) session.get(TroubledPhone.class, id);
		return tp;
	}

	public void removeTroubledPhone(Long id) {
		Session session = sessionFactory.getCurrentSession();
		TroubledPhone tp = getTroubledPhone(id);
		session.delete(tp);
	}

	public TroubledPhone getTroubledPhoneByNumber(String phoneNumber) {
		Session session = sessionFactory.getCurrentSession();
		TroubledPhone tp = (TroubledPhone) session.createCriteria(
				TroubledPhone.class).add(
				Restrictions.eq("phoneNumber", phoneNumber)).uniqueResult();
		return tp;
	}

	public MessageProgramEnrollment saveMessageProgramEnrollment(
			MessageProgramEnrollment enrollment) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(enrollment);
		return enrollment;
	}

	@SuppressWarnings("unchecked")
	public List<MessageProgramEnrollment> getAllActiveMessageProgramEnrollments() {
		Session session = sessionFactory.getCurrentSession();
		return (List<MessageProgramEnrollment>) session.createCriteria(
				MessageProgramEnrollment.class).add(
				Restrictions.isNotNull("startDate")).add(
				Restrictions.isNull("endDate")).list();
	}

	@SuppressWarnings("unchecked")
	public List<MessageProgramEnrollment> getActiveMessageProgramEnrollments(
			Integer personId) {
		Session session = sessionFactory.getCurrentSession();
		return (List<MessageProgramEnrollment>) session.createCriteria(
				MessageProgramEnrollment.class).add(
				Restrictions.eq("personId", personId)).add(
				Restrictions.isNotNull("startDate")).add(
				Restrictions.isNull("endDate")).list();
	}

	@SuppressWarnings("unchecked")
	public List<MessageProgramEnrollment> getActiveMessageProgramEnrollments(
			Integer personId, String program) {
		Session session = sessionFactory.getCurrentSession();
		return (List<MessageProgramEnrollment>) session.createCriteria(
				MessageProgramEnrollment.class).add(
				Restrictions.eq("personId", personId)).add(
				Restrictions.eq("program", program)).add(
				Restrictions.isNull("endDate")).list();
	}

	@SuppressWarnings("unchecked")
	public List<MessageProgramEnrollment> getActiveMessageProgramEnrollments(
			Integer personId, String program, Integer obsId) {
		Session session = sessionFactory.getCurrentSession();
		return (List<MessageProgramEnrollment>) session.createCriteria(
				MessageProgramEnrollment.class).add(
				Restrictions.eq("personId", personId)).add(
				Restrictions.eq("program", program)).add(
				Restrictions.eq("obsId", obsId)).add(
				Restrictions.isNull("endDate")).list();
	}

	public GeneralPatientEncounter saveGeneralPatientEncounter(
			GeneralPatientEncounter encounter) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(encounter);
		return encounter;
	}

	@SuppressWarnings("unchecked")
	public List<Location> getAllCountries() {
		Session session = sessionFactory.getCurrentSession();
		return (List<Location>) session.createCriteria(Location.class).add(
				Restrictions.isNotNull("country")).add(
				Restrictions.isNull("region")).add(
				Restrictions.isNull("countyDistrict")).add(
				Restrictions.isNull("cityVillage")).add(
				Restrictions.isNull("neighborhoodCell")).list();
	}

	@SuppressWarnings("unchecked")
	public List<Location> getAllRegions() {
		Session session = sessionFactory.getCurrentSession();
		return (List<Location>) session.createCriteria(Location.class).add(
				Restrictions.isNotNull("country")).add(
				Restrictions.isNotNull("region")).add(
				Restrictions.isNull("countyDistrict")).add(
				Restrictions.isNull("cityVillage")).add(
				Restrictions.isNull("neighborhoodCell")).list();
	}

	@SuppressWarnings("unchecked")
	public List<Location> getRegions(String country) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Location>) session.createCriteria(Location.class).add(
				Restrictions.eq("country", country)).add(
				Restrictions.isNotNull("region")).add(
				Restrictions.isNull("countyDistrict")).add(
				Restrictions.isNull("cityVillage")).add(
				Restrictions.isNull("neighborhoodCell")).list();
	}

	@SuppressWarnings("unchecked")
	public List<Location> getAllDistricts() {
		Session session = sessionFactory.getCurrentSession();
		return (List<Location>) session.createCriteria(Location.class).add(
				Restrictions.isNotNull("country")).add(
				Restrictions.isNotNull("region")).add(
				Restrictions.isNotNull("countyDistrict")).add(
				Restrictions.isNull("cityVillage")).add(
				Restrictions.isNull("neighborhoodCell")).list();
	}

	@SuppressWarnings("unchecked")
	public List<Location> getDistricts(String country, String region) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Location>) session.createCriteria(Location.class).add(
				Restrictions.eq("country", country)).add(
				Restrictions.eq("region", region)).add(
				Restrictions.isNotNull("countyDistrict")).add(
				Restrictions.isNull("cityVillage")).add(
				Restrictions.isNull("neighborhoodCell")).list();
	}

	@SuppressWarnings("unchecked")
	public List<Location> getAllCommunities() {
		Session session = sessionFactory.getCurrentSession();
		return (List<Location>) session.createCriteria(Location.class).add(
				Restrictions.isNotNull("country")).add(
				Restrictions.isNotNull("region")).add(
				Restrictions.isNotNull("countyDistrict")).add(
				Restrictions.isNotNull("cityVillage")).add(
				Restrictions.isNull("neighborhoodCell")).list();
	}

	@SuppressWarnings("unchecked")
	public List<Location> getCommunities(String country, String region,
			String district) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Location>) session.createCriteria(Location.class).add(
				Restrictions.eq("country", country)).add(
				Restrictions.eq("region", region)).add(
				Restrictions.eq("countyDistrict", district)).add(
				Restrictions.isNotNull("cityVillage")).add(
				Restrictions.isNull("neighborhoodCell")).list();
	}

	@SuppressWarnings("unchecked")
	public List<Location> getAllClinics() {
		Session session = sessionFactory.getCurrentSession();
		return (List<Location>) session.createCriteria(Location.class).add(
				Restrictions.isNotNull("country")).add(
				Restrictions.isNotNull("region")).add(
				Restrictions.isNotNull("countyDistrict")).add(
				Restrictions.isNotNull("cityVillage")).add(
				Restrictions.isNotNull("neighborhoodCell")).list();
	}

	@SuppressWarnings("unchecked")
	public List<Location> getClinics(String country, String region,
			String district, String community) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Location>) session.createCriteria(Location.class).add(
				Restrictions.eq("country", country)).add(
				Restrictions.eq("region", region)).add(
				Restrictions.eq("countyDistrict", district)).add(
				Restrictions.eq("cityVillage", community)).add(
				Restrictions.isNotNull("neighborhoodCell")).list();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getMatchingPeople(String firstName, String lastName,
			Date birthDate, String community, String phoneNumber,
			Integer primaryPhoneNumberAttrTypeId,
			Integer secondaryPhoneNumberAttrTypeId, String patientId,
			String nhisNumber, Integer nhisAttrTypeId) {
		Session session = sessionFactory.getCurrentSession();
		Date birthDateNextDay = null;
		if (birthDate != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(birthDate);
			calendar.add(Calendar.DATE, 1);
			birthDateNextDay = calendar.getTime();
		}
		return session
				.createSQLQuery(
						"select distinct p.person_id from person p "
								+ "left join person_name n on p.person_id = n.person_id "
								+ "left join person_address a on p.person_id = a.person_id "
								+ "left join person_attribute r on p.person_id = r.person_id "
								+ "left join patient_identifier i on p.person_id = i.patient_id "
								+ "where p.voided = 0 and n.voided = 0 and a.voided = 0 and "
								+ "r.voided = 0 and i.voided = 0 and "
								+ "(r.person_attribute_type_id = :nhisAttrTypeId and r.value = :nhisNumber) or "
								+ "(n.given_name = :firstName and n.family_name = :lastName and "
								+ "((p.birthdate >= :birthDate and p.birthdate < :birthDateNextDay) or "
								+ "a.city_village = :community or "
								+ "i.identifier = :patientId or "
								+ "(r.person_attribute_type_id = :primaryPhoneAttrTypeId and r.value = :phoneNumber or "
								+ "r.person_attribute_type_id = :secondaryPhoneAttrTypeId and r.value = :phoneNumber)))")
				.setInteger("nhisAttrTypeId", nhisAttrTypeId).setString(
						"nhisNumber", nhisNumber).setString("firstName",
						firstName).setString("lastName", lastName).setDate(
						"birthDate", birthDate).setDate("birthDateNextDay",
						birthDateNextDay).setString("community", community)
				.setString("patientId", patientId).setInteger(
						"primaryPhoneAttrTypeId", primaryPhoneNumberAttrTypeId)
				.setInteger("secondaryPhoneAttrTypeId",
						secondaryPhoneNumberAttrTypeId).setString(
						"phoneNumber", phoneNumber).list();
	}

	@SuppressWarnings("unchecked")
	public List<Obs> getActivePregnancies(Integer patientId,
			Concept pregnancyConcept, Concept pregnancyStatusConcept) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Obs>) session.createQuery(
				"select p from " + Obs.class.getName()
						+ " p where p.person.personId = :patientId and "
						+ "p.concept = :pregnancyConcept and "
						+ "exists (select s from " + Obs.class.getName()
						+ " s where s.obsGroup = p and "
						+ "s.concept = :pregnancyStatusConcept and "
						+ "s.valueNumeric = :trueDouble) and "
						+ "not exists (select e from " + Obs.class.getName()
						+ " e where e.obsGroup = p and "
						+ "e.concept = :pregnancyStatusConcept and "
						+ "e.valueNumeric = :falseDouble)").setInteger(
				"patientId", patientId).setEntity("pregnancyConcept",
				pregnancyConcept).setEntity("pregnancyStatusConcept",
				pregnancyStatusConcept).setDouble("trueDouble", 1.0).setDouble(
				"falseDouble", 0.0).list();
	}

	public Service saveService(Service service) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(service);
		return service;
	}

	@SuppressWarnings("unchecked")
	public List<Service> getServices(Integer patientId, String sequence,
			ServiceStatus status) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Service>) session.createCriteria(Service.class).add(
				Restrictions.eq("patientId", patientId)).add(
				Restrictions.eq("sequence", sequence)).add(
				Restrictions.eq("status", status)).list();
	}

	@SuppressWarnings("unchecked")
	public List<Service> getServices(Integer patientId, String service,
			String sequence, ServiceStatus status) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Service>) session.createCriteria(Service.class).add(
				Restrictions.eq("patientId", patientId)).add(
				Restrictions.eq("service", service)).add(
				Restrictions.eq("sequence", sequence)).add(
				Restrictions.eq("status", status)).list();
	}

	public ExpectedObs saveExpectedObs(ExpectedObs expectedObs) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(expectedObs);
		return expectedObs;
	}

	public void removeExpectedObs(ExpectedObs expectedObs) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(expectedObs);
	}

	@SuppressWarnings("unchecked")
	public List<ExpectedObs> getExpectedObs(Person person, Concept concept,
			Concept valueCoded, Double valueNumeric, Date obsDatetime) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ExpectedObs.class);
		if (person != null) {
			criteria.add(Restrictions.eq("person", person));
		}
		if (concept != null) {
			criteria.add(Restrictions.eq("concept", concept));
		}
		if (obsDatetime != null) {
			criteria.add(Restrictions.or(Restrictions.isNull("minObsDatetime"),
					Restrictions.le("minObsDatetime", obsDatetime)));
		}
		if (valueCoded != null) {
			criteria.add(Restrictions.eq("valueCoded", valueCoded));
		}
		if (valueNumeric != null) {
			criteria.add(Restrictions.eq("valueNumeric", valueNumeric));
		}
		return criteria.list();
	}

	public ExpectedEncounter saveExpectedEncounter(
			ExpectedEncounter expectedEncounter) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(expectedEncounter);
		return expectedEncounter;
	}

	public void removeExpectedEncounter(ExpectedEncounter expectedEncounter) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(expectedEncounter);
	}

	@SuppressWarnings("unchecked")
	public List<ExpectedEncounter> getExpectedEncounter(Patient patient,
			EncounterType encounterType, Date encounterDatetime) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ExpectedEncounter.class);
		if (patient != null) {
			criteria.add(Restrictions.eq("patient", patient));
		}
		if (encounterType != null) {
			criteria.add(Restrictions.eq("encounterType", encounterType));
		}
		if (encounterDatetime != null) {
			criteria.add(Restrictions.or(Restrictions
					.isNull("minEncounterDatetime"), Restrictions.le(
					"minEncounterDatetime", encounterDatetime)));
		}
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	public List<Patient> getPatients(String firstName, String lastName,
			String preferredName, Date birthDate, String community,
			String phoneNumber, PersonAttributeType primaryPhoneNumberAttrType,
			PersonAttributeType secondaryPhoneNumberAttrType,
			String nhisNumber, PersonAttributeType nhisAttrType) {

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(
				Patient.class);

		criteria.createAlias("names", "name");
		criteria.createAlias("addresses", "addr");
		criteria.createAlias("attributes", "att");

		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.eq("name.voided", false));
		criteria.add(Restrictions.eq("addr.voided", false));
		criteria.add(Restrictions.eq("att.voided", false));

		Criterion nhisCriterion = Restrictions.and(Restrictions.eq(
				"att.attributeType", nhisAttrType), Restrictions.eq(
				"att.value", nhisNumber));
		Criterion nameCriterion = Restrictions.or(Restrictions.and(Restrictions
				.eq("name.givenName", firstName), Restrictions.eq(
				"name.familyName", lastName)), Restrictions.and(Restrictions
				.eq("name.givenName", preferredName), Restrictions.eq(
				"name.familyName", lastName)));
		Criterion phoneCriterion = Restrictions.or(Restrictions.and(
				Restrictions
						.eq("att.attributeType", primaryPhoneNumberAttrType),
				Restrictions.eq("att.value", phoneNumber)), Restrictions.and(
				Restrictions.eq("att.attributeType",
						secondaryPhoneNumberAttrType), Restrictions.eq(
						"att.value", phoneNumber)));
		Disjunction otherCriterion = Restrictions.disjunction();
		otherCriterion.add(Restrictions.eq("birthdate", birthDate));
		otherCriterion.add(Restrictions.eq("addr.cityVillage", community));
		otherCriterion.add(phoneCriterion);

		// Get Patients by NHIS or
		// (((FirstName and LastName) or (PreferredName and LastName)) and
		// (BirthDate or Community or (PrimaryPhone or SecondaryPhone))))
		criteria.add(Restrictions.or(nhisCriterion, Restrictions.and(
				nameCriterion, otherCriterion)));

		criteria.addOrder(Order.asc("name.givenName"));
		criteria.addOrder(Order.asc("name.familyName"));
		criteria.addOrder(Order.asc("birthdate"));
		criteria.addOrder(Order.asc("addr.cityVillage"));

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return criteria.list();
	}
}
