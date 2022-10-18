package ru.job4j.quartz;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    private static final Logger LOG = LogManager.getLogger(AlertRabbit.class.getName());


    private static Connection init(Properties cfg) throws ClassNotFoundException, SQLException {

        Class.forName(cfg.getProperty("driver-class-name"));

        return DriverManager.getConnection(cfg.getProperty("url"), cfg.getProperty("username"), cfg.getProperty("password"));

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
            Properties cfg = read();
            try (Connection connection = init(cfg)) {
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                JobDataMap data = new JobDataMap();
                data.put("connection", connection);
                JobDetail job = newJob(Rabbit.class)
                        .usingJobData(data)
                        .build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("rabbit.interval")))
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10000);
                scheduler.shutdown();
            }
        } catch (Exception se) {
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