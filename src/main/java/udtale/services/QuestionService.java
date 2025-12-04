package udtale.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import udtale.dto.PagedResponse;
import udtale.dto.QuestionSearchRequest;
import udtale.models.Question;
import udtale.repositories.QuestionRepository;

import java.util.Objects;

@Slf4j
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    private boolean hasSearchCriteria(QuestionSearchRequest request) {
        return (
                Objects.nonNull(request.getSearch()) && !request.getSearch().trim().isEmpty() ||
                Objects.nonNull(request.getField()) && !request.getField().trim().isEmpty() ||
                Objects.nonNull(request.getGroup()) && !request.getGroup().trim().isEmpty()
        );
    }

    private PagedResponse<Question> searchQuestions(QuestionSearchRequest request, Pageable pageable) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        String search = request.getSearch().trim();


//        build individual criteria based on the coming request
        if (Objects.nonNull(request.getSearch()) && !search.isEmpty()) {

            criteria = criteria.orOperator(
                    Criteria.where("field").regex(search, "i"),
                    Criteria.where("group").regex(search, "i"),
                    Criteria.where("question").regex(search, "i")
            );

        }
//
//        if (request.getSearch() && !request.getField().trim().isEmpty()) {
//            criteria = criteria.and("field").regex(request.getSearch());
//        }

        return null;

    }




    public PagedResponse<Question> getDocuments(QuestionSearchRequest searchRequest, String sessionId) {
        Sort sort = Sort.by(searchRequest.getSortDirection(), searchRequest.getSortBy());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        if (hasSearchCriteria(searchRequest)) {
            return null;
        }

        log.info("");
        return null;
    }

}
