package org.example.wtdapp.service;

import org.example.wtdapp.entity.Task;
import org.example.wtdapp.entity.User;
import org.example.wtdapp.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task taskDetails) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            Task existingTask = task.get();
            existingTask.setTitle(taskDetails.getTitle());
            existingTask.setDescription(taskDetails.getDescription());
            existingTask.setStartDate(taskDetails.getStartDate());
            existingTask.setEndDate(taskDetails.getEndDate());
            existingTask.setStartTime(taskDetails.getStartTime());
            existingTask.setEndTime(taskDetails.getEndTime());
            existingTask.setCategory(taskDetails.getCategory());
            existingTask.setCompleted(taskDetails.getCompleted());
            existingTask.setPriority(taskDetails.getPriority());
            existingTask.setRecurrenceType(taskDetails.getRecurrenceType());
            return taskRepository.save(existingTask);
        }
        return null;
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> getTasksByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return taskRepository.findTasksByUserAndDateRange(user, startDate, endDate);
    }

    public List<Task> getTasksByUserAndDate(User user, LocalDate date) {
        return taskRepository.findTasksByUserAndDate(user, date);
    }

    public List<Task> getTasksByUser(User user) {
        return taskRepository.findAllTasksForUser(user);
    }

    public Task shareTaskWithUser(Long taskId, User user) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent()) {
            Task existingTask = task.get();
            existingTask.getSharedWith().add(user);
            return taskRepository.save(existingTask);
        }
        return null;
    }

    public Task removeTaskShare(Long taskId, User user) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent()) {
            Task existingTask = task.get();
            existingTask.getSharedWith().remove(user);
            return taskRepository.save(existingTask);
        }
        return null;
    }
}
