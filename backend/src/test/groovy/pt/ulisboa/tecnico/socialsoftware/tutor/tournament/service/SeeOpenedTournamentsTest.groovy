package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class SeeOpenedTournamentsTest extends Specification {

    public static final String USER_NAME = "Dinis"
    public static final String USERNAME = "JDinis99"
    public static final Integer KEY = 1
    public static final String COURSE_NAME = "Software Architecture"
    public static final String TOPIC_NAME1 = "Informática"
    public static final String TOPIC_NAME2 = "Engenharia de Software"
    public static final int NUMBER_OF_QUESTIONS1 = 5
    public static final int NUMBER_OF_QUESTIONS2 = 7

    @Autowired
    TournamentService tournamentService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TopicRepository topicRepository

    def user
    def course
    def topic1
    def topic2
    def topicDto1
    def topicDto2
    def topics1 = new ArrayList<Integer>()
    def topics2 = new ArrayList<Integer>()
    def startTime_Now = LocalDateTime.now()
    def endTime_Now = LocalDateTime.now().plusHours(2)

    def startTime_Later = LocalDateTime.now().plusHours(1)
    def endTime_Later = LocalDateTime.now().plusHours(2)

    def setup() {
        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        topicDto1 = new TopicDto()
        topicDto1.setName(TOPIC_NAME1)
        topic1 = new Topic(course, topicDto1)
        topicRepository.save(topic1)

        topicDto2 = new TopicDto()
        topicDto2.setName(TOPIC_NAME2)
        topic2 = new Topic(course, topicDto2)
        topicRepository.save(topic2)

        topics1.add(topic1.getId())
        topics2.add(topic2.getId())



    }

    def "create 2 tournaments on time and see opened tournaments"() {
        given:
        def tournamentDto1 = new TournamentDto()
        tournamentDto1.setStartTime(startTime_Now)
        tournamentDto1.setEndTime(endTime_Now)
        tournamentDto1.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDto1.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics1, tournamentDto1)

        and:
        def tournamentDto2 = new TournamentDto()
        tournamentDto2.setStartTime(startTime_Now)
        tournamentDto2.setEndTime(endTime_Now)
        tournamentDto2.setNumberOfQuestions(NUMBER_OF_QUESTIONS2)
        tournamentDto2.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics2, tournamentDto2)

        when:
        def result = tournamentService.seeOpenedTournaments()

        then: "the returned data is correct"
        result.size() == 2
        def resTournament1 = result.get(0)
        def resTournament2 = result.get(1)

        resTournament1.getStartTime() == tournamentDto1.getStartTime()
        resTournament2.getEndTime() == tournamentDto1.getEndTime()
        resTournament1.getNumberOfQuestions() == tournamentDto1.getNumberOfQuestions()
        resTournament1.getState() == tournamentDto1.getState()
        def topicsResults1 = resTournament1.getTopics()
        topicsResults1.get(0).getName() == TOPIC_NAME1

        resTournament2.getStartTime() == tournamentDto2.getStartTime()
        resTournament2.getEndTime() == tournamentDto2.getEndTime()
        resTournament2.getNumberOfQuestions() == tournamentDto2.getNumberOfQuestions()
        resTournament2.getState() == tournamentDto2.getState()
        def topicsResults2 = resTournament2.getTopics()
        topicsResults2.get(0).getName() == TOPIC_NAME2

    }


    def "create 2 tournaments on time and 1 out of time and see opened tournaments"() {
        given:
        def tournamentDto1 = new TournamentDto()
        tournamentDto1.setStartTime(startTime_Now)
        tournamentDto1.setEndTime(endTime_Now)
        tournamentDto1.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDto1.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics1, tournamentDto1)

        and:
        def tournamentDto2 = new TournamentDto()
        tournamentDto2.setStartTime(startTime_Now)
        tournamentDto2.setEndTime(endTime_Now)
        tournamentDto2.setNumberOfQuestions(NUMBER_OF_QUESTIONS2)
        tournamentDto2.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics2, tournamentDto2)

        and:
        def tournamentDto3 = new TournamentDto()
        tournamentDto3.setStartTime(startTime_Later)
        tournamentDto3.setEndTime(endTime_Later)
        tournamentDto3.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDto3.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics1, tournamentDto3)

        when:
        def result = tournamentService.seeOpenedTournaments()


        then: "the returned data is correct"
        result.size() == 2
        def resTournament1 = result.get(0)
        def resTournament2 = result.get(1)

        resTournament1.getStartTime() == tournamentDto1.getStartTime()
        resTournament2.getEndTime() == tournamentDto1.getEndTime()
        resTournament1.getNumberOfQuestions() == tournamentDto1.getNumberOfQuestions()
        resTournament1.getState() == tournamentDto1.getState()
        def topicsResults1 = resTournament1.getTopics()
        topicsResults1.get(0).getName() == TOPIC_NAME1

        resTournament2.getStartTime() == tournamentDto2.getStartTime()
        resTournament2.getEndTime() == tournamentDto2.getEndTime()
        resTournament2.getNumberOfQuestions() == tournamentDto2.getNumberOfQuestions()
        resTournament2.getState() == tournamentDto2.getState()
        def topicsResults2 = resTournament2.getTopics()
        topicsResults2.get(0).getName() == TOPIC_NAME2

    }

    def "create 2 tournaments on time and 1 canceled and see opened tournaments"() {
        given:
        def tournamentDto1 = new TournamentDto()
        tournamentDto1.setStartTime(startTime_Now)
        tournamentDto1.setEndTime(endTime_Now)
        tournamentDto1.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDto1.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics1, tournamentDto1)

        and:
        def tournamentDto2 = new TournamentDto()
        tournamentDto2.setStartTime(startTime_Now)
        tournamentDto2.setEndTime(endTime_Now)
        tournamentDto2.setNumberOfQuestions(NUMBER_OF_QUESTIONS2)
        tournamentDto2.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics2, tournamentDto2)

        and:
        def tournamentDto3 = new TournamentDto()
        tournamentDto3.setStartTime(startTime_Now)
        tournamentDto3.setEndTime(endTime_Now)
        tournamentDto3.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDto3.setState(Tournament.Status.CANCELED)
        tournamentService.createTournament(user.getUsername(), topics1, tournamentDto3)

        when:
        def result = tournamentService.seeOpenedTournaments()

        then: "the returned data is correct"
        result.size() == 2
        def resTournament1 = result.get(0)
        def resTournament2 = result.get(1)

        resTournament1.getStartTime() == tournamentDto1.getStartTime()
        resTournament2.getEndTime() == tournamentDto1.getEndTime()
        resTournament1.getNumberOfQuestions() == tournamentDto1.getNumberOfQuestions()
        resTournament1.getState() == tournamentDto1.getState()
        def topicsResults1 = resTournament1.getTopics()
        topicsResults1.get(0).getName() == TOPIC_NAME1

        resTournament2.getStartTime() == tournamentDto2.getStartTime()
        resTournament2.getEndTime() == tournamentDto2.getEndTime()
        resTournament2.getNumberOfQuestions() == tournamentDto2.getNumberOfQuestions()
        resTournament2.getState() == tournamentDto2.getState()
        def topicsResults2 = resTournament2.getTopics()
        topicsResults2.get(0).getName() == TOPIC_NAME2

    }

    def "create 0 tournaments and see opened tournaments"() {
        given:

        when:
        def result = tournamentService.seeOpenedTournaments()

        then: "there is no returned data"
        result.size() == 0
    }

    def "create one out of time and see opened tournaments"() {
        given:
        def tournamentDto3 = new TournamentDto()
        tournamentDto3.setStartTime(startTime_Later)
        tournamentDto3.setEndTime(endTime_Later)
        tournamentDto3.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDto3.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics1, tournamentDto3)

        when:
        def result = tournamentService.seeOpenedTournaments()

        then: "there is no returned data"
        result.size() == 0
    }

    def "create one canceled and see opened tournaments"() {
        given:
        def tournamentDto3 = new TournamentDto()
        tournamentDto3.setStartTime(startTime_Now)
        tournamentDto3.setEndTime(endTime_Now)
        tournamentDto3.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDto3.setState(Tournament.Status.CANCELED)
        tournamentService.createTournament(user.getUsername(), topics1, tournamentDto3)

        when:
        def result = tournamentService.seeOpenedTournaments()

        then: "there is no returned data"
        result.size() == 0
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {
        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

}