package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.ReplyRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.CryptoService
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.ServerKeys
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.WorkerService
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import java.time.LocalDateTime


@DataJpaTest
class CreateReplyTest extends Specification {
    public static final String ACADEMIC_TERM = "academic term"
    public static final String ACRONYM = "acronym"
    public static final String COURSE_NAME = "course name"
    public static final String DISCUSSION_REPLY = "discussion reply"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    DiscussionService discussionService

    @Autowired
    DiscussionRepository discussionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuestionAnswerRepository questionAnswerRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository;

    @Autowired
    QuizQuestionRepository quizQuestionRepository;

    @Autowired
    UserRepository userRepository

    @Autowired
    ReplyRepository replyRepository

    def course
    def courseExecution
    def teacher
    def student
    def question
    def discussion

    def setup() {
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)

        teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 1, User.Role.TEACHER)
        userRepository.save(teacher)
        student = new User(USER_NAME, USER_USERNAME, 2, User.Role.STUDENT)

        def quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType("TEST")

        def quizanswer = new QuizAnswer()

        def questionanswer = new QuestionAnswer()
        questionanswer.setTimeTaken(1)
        def quizquestion = new QuizQuestion(quiz, question, 3)
        questionanswer.setQuizQuestion(quizquestion)
        questionanswer.setQuizAnswer(quizanswer)
        questionAnswerRepository.save(questionanswer)

        quizquestion.addQuestionAnswer(questionanswer)
        quizanswer.addQuestionAnswer(questionanswer)

        quizQuestionRepository.save(quizquestion)
        quizAnswerRepository.save(quizanswer)

        quiz.addQuizAnswer(quizanswer)
        quiz.addQuizQuestion(quizquestion)

        quizRepository.save(quiz)

        questionRepository.save(question)
        student.addQuizAnswer(quizanswer)
        userRepository.save(student)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecution.addUser(student)
        courseExecution.addUser(teacher)
        courseExecutionRepository.save(courseExecution)

        student.addCourse(courseExecution)
        teacher.addCourse(courseExecution)
        userRepository.save(student)
        userRepository.save(teacher)

        discussion = new Discussion()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUser(student)
        discussion.setQuestion(question)
        discussion.setCourse(course)
        discussionRepository.save(discussion)
        userRepository.save(student)
    }

    def "teacher give reply to discussion"(){
        given: "a reply"
        def replyDto = new ReplyDto()
        replyDto.setMessage(DISCUSSION_REPLY)
        replyDto.setUserId(teacher.getId())
        replyDto.setDate(LocalDateTime.now())


        when: "a reply is given"
        discussionService.createReply(replyDto, new DiscussionDto(discussion))

        then: "the correct reply was given"
        replyRepository.count() == 1L
        def result = replyRepository.findAll().get(0)
        result.getMessage() == DISCUSSION_REPLY
        result.getUser() == teacher
    }

    def "student give reply to his discussion"(){
        given: "a response created by a student"
        def replyDto = new ReplyDto()
        replyDto.setMessage(DISCUSSION_REPLY)
        replyDto.setUserId(student.getId())
        replyDto.setDate(LocalDateTime.now())

        when: "the student creates a reply"
        discussionService.createReply(replyDto, new DiscussionDto(discussion))

        then: "the correct reply was given"
        replyRepository.count() == 1L
        def result = replyRepository.findAll().get(0)
        result.getMessage() == DISCUSSION_REPLY
        result.getUser() == student
    }

    def "student not creator give reply to discussion"(){
        given: "a different student"
        def other = new User(USER_NAME + "2", USER_USERNAME + "2", 3, User.Role.STUDENT)
        userRepository.save(other)
        courseExecution.addUser(other)
        courseExecutionRepository.save(courseExecution)
        other.addCourse(courseExecution)
        userRepository.save(other)
        and: "a reply created by the student"
        def replyDto = new ReplyDto()
        replyDto.setMessage(DISCUSSION_REPLY)
        replyDto.setUserId(other.getId())
        replyDto.setDate(LocalDateTime.now())

        when: "the student creates a reply"
        discussionService.createReply(replyDto, new DiscussionDto(discussion))

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.REPLY_UNAUTHORIZED_USER
    }

    def "user can submit 2 replies to the same discussion"(){
        given: "2 replies from the same user"
        def replyDto = new ReplyDto()
        replyDto.setMessage(DISCUSSION_REPLY)
        replyDto.setUserId(teacher.getId())
        replyDto.setDate(LocalDateTime.now())
        def replyDto2 = new ReplyDto()
        replyDto2.setMessage(DISCUSSION_REPLY + "2")
        replyDto2.setUserId(teacher.getId())
        replyDto2.setDate(LocalDateTime.now())
        discussionService.createReply(replyDto, new DiscussionDto(discussion))

        when: "another reply is given"
        discussionService.createReply(replyDto2, new DiscussionDto(discussion))

        then: "the two replies are present in the repository"
        replyRepository.count() == 2L
        def result = replyRepository.findAll().get(0)
        result.getMessage() == DISCUSSION_REPLY
        result.getUser() == teacher
        def result2 = replyRepository.findAll().get(1)
        result2.getMessage() == (DISCUSSION_REPLY + "2")
        result2.getUser() == teacher
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {
        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }

        @Bean
        NotificationService notificationService() {
            return new NotificationService()
        }

        @Bean
        WorkerService workerService() {
            return new WorkerService()
        }

        @Bean
        CryptoService cryptoService() {
            return new CryptoService()
        }

        @Bean
        ServerKeys serverKeys() {
            return new ServerKeys()
        }
    }
}
