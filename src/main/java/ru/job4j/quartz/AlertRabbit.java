package ru.job4j.quartz;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    private static final Logger LOG = LogManager.getLogger(AlertRabbit.class.getName());


    private static Connection init() {
        Connection cn;
        try {
            Class.forName(read().getProperty("driver-class-name"));
            cn = DriverManager.getConnection(
                    read().getProperty("url"),
                    read().getProperty("username"),
                    read().getProperty("password")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    return cn;

    }
    private static Properties read() {
        Properties cfg = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            LOG.info(e.getMessage());
        }
        return cfg;
    }

    public static void main(String[] args) {

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", init());
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(read().getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception  se) {
            LOG.info(se.getMessage());
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }
        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");

                try (PreparedStatement ps = connection.prepareStatement("insert into rabbit (created_date) values (?)")) {
                    ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    ps.execute();
                } catch (SQLException e) {
                    LOG.info(e.getMessage());
                }

        }
    }
}