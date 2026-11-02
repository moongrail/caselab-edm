package ru.caselab.edm.platform.search;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "platform.search")
public class SearchProperties {

    private boolean reindexOnStartup = false;
}
