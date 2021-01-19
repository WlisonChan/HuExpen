package org.csu.mcs;

import lombok.extern.slf4j.Slf4j;
import org.csu.algorithm.DecoyStage;
import org.csu.algorithm.SinkStage;
import org.csu.entity.Agent;
import org.csu.entity.Task;
import org.csu.type.TaskType;

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
        SinkStage.build(agentList);
    }

    /**
     * 初始化参与者参数
     *
     * @return
     */
    public static List<Agent> initAgentStatus() {
        log.info("-----开始参与者初始化-----");
        List<Agent> agentList = new ArrayList<>();
        for (int i = 0; i < Constant.WORKER_NUM; i++) {
            double sinkMax = generateRandom(Constant.SINK_MAX_FLOOR, Constant.SINK_MAX_UPPER);
            double costUpper = generateRandom(Constant.AGENT_COST_FLOOR, Constant.AGENT_COST_UPPER);
            double sensorCost = generateRandom(Constant.SENSOR_COST_FLOOR, Constant.SENSOR_COST_UPPER);
            double willingness = generateRandom(Constant.WILLINGNESS_FLOOR, Constant.WILLINGNESS_UPPER);
            double participantVal = generateRandom(Constant.PARTICIPATION_FLOOR, Constant.PARTICIPATION_UPPER);

            Agent agent = new Agent();
            agent.setAgentId(i)
                    .setSinkMax(sinkMax)
                    .setCostUpper(costUpper)
                    .setSensoryCost(sensorCost)
                    .setWillingness(willingness)
                    .setVThreshold(participantVal);
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
            double quality = generateRandom(Constant.TASK_QUALITY_FLOOR, Constant.TASK_QUALITY_UPPER);

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

    /**
     * 根据上下限生成随机值
     *
     * @param floor
     * @param upper
     * @return
     */
    public static double generateRandom(double floor, double upper) {
        Random random = new Random();
        return floor + (upper - floor) * random.nextDouble();
    }

}
