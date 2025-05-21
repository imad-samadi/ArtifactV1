package com.S2M.ArtifactTest.Config.ReadDb.Providers;

import com.S2M.ArtifactTest.Config.ReadDb.DatabaseReaderProperties;
import com.S2M.ArtifactTest.Config.ReadDb.SPI.DatabaseReaderProvider;
import com.S2M.ArtifactTest.Config.ReadFile.Core.Exceptions.ConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class RepositoryItemReaderProvider<T> implements DatabaseReaderProvider<T> {

    private final ApplicationContext ctx;
    private final DatabaseReaderProperties props;
    private final Class<T> entityType;

    @Override
    public ItemReader<T> createReader() {
        log.info("Creating RepositoryItemReader for repository: '{}', method: '{}', entity: {}",
                props.getServiceBeanName(), props.getServiceMethodName(), entityType.getSimpleName());

        PagingAndSortingRepository<?, ?> repository;
        try {
            repository = (PagingAndSortingRepository<?, ?>) ctx.getBean(props.getServiceBeanName());
        } catch (NoSuchBeanDefinitionException e) {
            throw new ConfigurationException("Repository bean (serviceBeanName: '" + props.getServiceBeanName() + "') not found.", e);
        } catch (ClassCastException e) {
            throw new ConfigurationException("Bean (serviceBeanName: '" + props.getServiceBeanName() +
                    "') is not a PagingAndSortingRepository.", e);
        }

        RepositoryItemReaderBuilder<T> builder = new RepositoryItemReaderBuilder<T>()
                .name("repositoryItemReader." + entityType.getSimpleName() + "." + props.getServiceMethodName())
                .repository(repository)
                .methodName(props.getServiceMethodName())
                .pageSize(props.getPageSize());

        List<Object> arguments;
        if (props.getRepositoryMethodArguments() != null && !props.getRepositoryMethodArguments().isEmpty()) {
            arguments = new ArrayList<>(props.getRepositoryMethodArguments());
            log.debug("Using dedicated repositoryMethodArguments: {}", arguments);
        } else if (props.getParameterValues() != null && !props.getParameterValues().isEmpty()) {
            // Fallback to parameterValues (order-dependent and fragile)
            arguments = new ArrayList<>(props.getParameterValues().values());
            log.warn("Using fallback parameterValues for arguments (order-dependent!): {}. Consider using 'repositoryMethodArguments'.", arguments);
        } else {
            arguments = Collections.emptyList();
        }
        builder.arguments(arguments);


        Sort springDataSort = props.getRepositorySpringSort();
        if (springDataSort.isSorted()) {

            Map<String, Sort.Direction> sortsMap = new LinkedHashMap<>();
            springDataSort.forEach(order -> sortsMap.put(order.getProperty(), order.getDirection()));
            if(!sortsMap.isEmpty()){
                log.debug("Applying sorts to RepositoryItemReader: {}", sortsMap);
                builder.sorts(sortsMap);
            }
        } else {
            log.debug("No explicit sorts provided for RepositoryItemReader. Relies on method/default sort.");
        }

        return builder.build();
    }
}
