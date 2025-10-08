package com.bervan.asynctask;

import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.service.AuthService;
import com.bervan.common.service.BaseService;
import com.bervan.common.user.User;
import com.bervan.history.model.BaseRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AsyncTaskService extends BaseService<UUID, AsyncTask> {
    private static AsyncTaskService instance;

    protected AsyncTaskService(BaseRepository<AsyncTask, UUID> repository, SearchService searchService) {
        super(repository, searchService);
    }

    public static List<AsyncTask> getTaskNotificationsForUser() {
        UUID loggedUserId = AuthService.getLoggedUserId();
        if (loggedUserId == null) {
            return Collections.emptyList();
        }
        return instance.getTaskNotificationsForUser(loggedUserId);
    }

    public synchronized static void updateStateToNotified(AsyncTask asyncTask) {
        asyncTask.setNotified(true);
        instance.save(asyncTask);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void timeoutCheck() {
        log.info("Starting timeout check");
        LocalDateTime now = LocalDateTime.now();
        Set<AsyncTask> asyncTasks = queryNotFinishedTasks();
        for (AsyncTask asyncTask : asyncTasks) {
            if (asyncTask.getStartDate() != null && asyncTask.getTimeoutInMin() != null && asyncTask.getStartDate().plusMinutes(asyncTask.getTimeoutInMin()).isBefore(now)) {
                log.info("Timeout for task {} reached", asyncTask.getId());
                setFailed(asyncTask, "Timeout reached");
            }
        }
    }

    @PostConstruct
    private void init() {
        instance = this;
    }

    @Override
    public AsyncTask save(AsyncTask data) {
        Set<UUID> ownerIds = data.getOwners().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        AsyncTask saved = super.save(data);

        for (UUID ownerId : ownerIds) {
            evictCacheForUser(ownerId);
        }

        return saved;

    }

    @CacheEvict(value = "taskNotifications", key = "#userId")
    public void evictCacheForUser(UUID userId) {

    }

    @Cacheable(value = "taskNotifications", key = "#userId")
    public List<AsyncTask> getTaskNotificationsForUser(UUID userId) {
        return queryTaskNotificationFromDatabase().getOrDefault(userId, Collections.emptyList());
    }

    private Set<AsyncTask> queryNotFinishedTasks() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setAddOwnerCriterion(false);
        searchRequest.addCriterion("TASK_NOTIFICATION_GROUP", Operator.OR_OPERATOR, AsyncTask.class, "status", SearchOperation.EQUALS_OPERATION, "NEW");
        searchRequest.addCriterion("TASK_NOTIFICATION_GROUP", Operator.OR_OPERATOR, AsyncTask.class, "status", SearchOperation.EQUALS_OPERATION, "IN_PROGRESS");

        return load(searchRequest, Pageable.ofSize(50000));
    }

    private Map<UUID, List<AsyncTask>> queryTaskNotificationFromDatabase() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setAddOwnerCriterion(false);
        searchRequest.addCriterion("TASK_NOTIFICATION_GROUP", Operator.AND_OPERATOR, AsyncTask.class, "notified", SearchOperation.NOT_EQUALS_OPERATION, true);
        searchRequest.addCriterion("TASK_NOTIFICATION_GROUP", Operator.AND_OPERATOR, AsyncTask.class, "status", SearchOperation.NOT_EQUALS_OPERATION, "NEW");
        searchRequest.addCriterion("TASK_NOTIFICATION_GROUP", Operator.AND_OPERATOR, AsyncTask.class, "status", SearchOperation.NOT_EQUALS_OPERATION, "IN_PROGRESS");

        Set<AsyncTask> loaded = load(searchRequest, Pageable.ofSize(50000));

        return loaded.stream()
                .flatMap(task -> task.getOwners().stream()
                        .map(user -> Map.entry(user.getId(), task)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

    }

    public AsyncTask createAndStoreAsyncTask() {
        AsyncTask newAsyncTask = new AsyncTask();
        LocalDateTime now = LocalDateTime.now();
        newAsyncTask.setCreationDate(now);
        newAsyncTask.setNotified(false);
        newAsyncTask.setNotifyOnSuccess(true);
        newAsyncTask.setModificationDate(now);
        newAsyncTask.setStatus("NEW");

        return save(newAsyncTask);
    }

    public AsyncTask setInProgress(AsyncTask asyncTask, String message) {
        asyncTask.setStatus("IN_PROGRESS");
        LocalDateTime now = LocalDateTime.now();
        asyncTask.setStartDate(now);
        asyncTask.setModificationDate(now);
        asyncTask.setMessage(message);
        return save(asyncTask);
    }

    public void setFinished(AsyncTask asyncTask, String message) {
        asyncTask.setStatus("FINISHED");
        LocalDateTime now = LocalDateTime.now();
        asyncTask.setModificationDate(now);
        asyncTask.setEndDate(now);
        asyncTask.setMessage(message);
        save(asyncTask);
    }

    public void setFailed(AsyncTask asyncTask, String message) {
        asyncTask.setStatus("FAILED");
        LocalDateTime now = LocalDateTime.now();
        asyncTask.setModificationDate(now);
        asyncTask.setEndDate(now);
        asyncTask.setMessage(message);
        save(asyncTask);
    }
}
