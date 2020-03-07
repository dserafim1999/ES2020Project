package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class TournamentService {
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TopicRepository topicRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(String username, List<Integer> topicsId, TournamentDto tournamentDto) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new TutorException(USER_NOT_FOUND, username);
        }

        // Added 10 seconds as a buffer to take latency into consideration
        if (!tournamentDto.getStartTime().isBefore(tournamentDto.getEndTime())
            || tournamentDto.getStartTime().plusSeconds(10).isBefore(LocalDateTime.now())) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "date");
        }

        if (tournamentDto.getNumberOfQuestions() <= 0) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "number of questions");
        }

        List<Topic> topics = new ArrayList<>();
        for (Integer topicId : topicsId) {
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, topicId));
            topics.add(topic);
        }

        if (topics.size() == 0) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "topics");
        }

        Tournament tournament = new Tournament(user, topics, tournamentDto);
        this.entityManager.persist(tournament);
        return new TournamentDto(tournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    // Tournament DTO?
    public List<TournamentDto> seeOpenedTournaments() {
        return tournamentRepository.seeOpenedTournaments().stream().map(TournamentDto::new).collect(Collectors.toList());
                // TODO throw exeptions?
    }

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeTopic(Integer topicId, TournamentDto tournamentDto) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, topicId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                        .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));
        tournament.removeTopic(topic);
    }
}