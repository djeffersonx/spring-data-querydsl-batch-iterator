package br.com.djeffersonx.batch;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, TaskExecutionAutoConfiguration.class})
public class TestApplication {
}