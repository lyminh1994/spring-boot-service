package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import io.spring.application.TagsQueryService;
import io.spring.graphql.DgsConstants.QUERY;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DgsComponent
@RequiredArgsConstructor
public class TagDataFetcher {
    private final TagsQueryService tagsQueryService;

    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Tags)
    public List<String> getTags() {
        return tagsQueryService.allTags();
    }
}
