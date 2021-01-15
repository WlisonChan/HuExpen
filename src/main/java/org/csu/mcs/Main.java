package org.csu.mcs;

import lombok.extern.slf4j.Slf4j;
import org.csu.algorithm.DecoyStage;
import org.csu.entity.Agent;
import org.csu.entity.Task;
import org.csu.entity.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class Main {

    // 当前平均质量
    public static double avgQ = 0;

    public static void main(String[] args) {
        List<Agent> agentList = initAgentStatus();
        List<Task> taskList = initTaskStatus();
        classifyTask(taskList);
        DecoyStage.build(taskList, agentList);
    }

    /**
     * 初始化参与者参数
     *
     * @return
     */
    public static List<Agent> initAgentStatus() {
        log.info("-----开始参与者初始化-----");
        List<Agent> agentList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < Constant.WORKER_NUM; i++) {
            double costBase = Constant.AGENT_COST_PERCENT * Constant.AGENT_COST_UPPER;
            double costUpper = costBase + (Constant.AGENT_COST_UPPER - costBase) * random.nextDouble();
            double sensorCost = Constant.SENSOR_COST_FLOOR +
                    (Constant.SENSOR_COST_UPPER - Constant.SENSOR_COST_FLOOR) * random.nextDouble();
            double willingness = Constant.WILLINGNESS_FLOOR +
                    (Constant.WILLINGNESS_UPPER - Constant.WILLINGNESS_FLOOR) * random.nextDouble();

            Agent agent = new Agent();
            agent.setAgentId(i)
                    .setCostUpper(costUpper)
                    .setSensoryCost(sensorCost)
                    .setWillingness(willingness);
            agentList.add(agent);
            log.info("参与者信息：{}", agent);
        }
        log.info("-----参与者初始化结束-----");
        return agentList;
    }

    /**
     * 初始化任务参数
     *
     * @return
     */
    public static List<Task> initTaskStatus() {
        log.info("-----开始任务初始化-----");
        List<Task> taskList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < Constant.TASK_NUM; i++) {
            double qualityBase = Constant.TASK_QUALITY_UPPER * Constant.TASK_QUALITY_PERCENT;
            double quality = qualityBase + (Constant.TASK_QUALITY_UPPER - qualityBase) * random.nextDouble();

            Task task = new Task();
            task.setTaskId(i)
                    .setTaskQuality(quality)
                    .setTaskValue(Constant.getTaskValue(quality))
                    .setTaskBid(Constant.getTaskBid(task.getTaskValue()));

            taskList.add(task);
            log.info("任务信息：{}", task);
        }
        log.info("-----任务初始化结束-----");
        return taskList;
    }

    /**
     * 任务分类
     *
     * @param taskList
     */
    public static void classifyTask(List<Task> taskList) {

        // 计算平均质量
        double avg = taskList.stream()
                .mapToDouble(Task::getTaskQuality)
                .average()
                .orElse(0D);

        avgQ = avg;

        // 任务分类
        taskList.forEach(e -> {
            if (e.getTaskQuality() > avg) {
                e.setTaskType(TaskType.Target);
            } else {
                e.setTaskType(TaskType.Compete);
            }
        });
    }

}
