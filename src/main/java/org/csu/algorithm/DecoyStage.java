package org.csu.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.csu.entity.Agent;
import org.csu.entity.Task;
import org.csu.entity.TaskType;
import org.csu.mcs.Constant;

import java.util.*;

@Slf4j
public class DecoyStage {

    // 诱饵任务与目标任务的质量转化比
    public static final double DECOY_QUALITY_TRANSFER = 0.99;

    // 诱饵任务与目标任务的报价转化比
    public static final double DECOY_BID_TRANSFER = 0.93;

    /**
     * 添加诱饵任务
     * @param taskList
     */
    public List<Task> addDecoyTask(List<Task> taskList){
        log.info("-----诱饵任务加入-----");
        List<Task> decoyTask = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            if (TaskType.Target.equals(task.getTaskType())){
                Task decoy = new Task();
                decoy.setTaskQuality(task.getTaskQuality() * DECOY_QUALITY_TRANSFER)
                        .setTaskBid(task.getTaskBid() * DECOY_BID_TRANSFER)
                        .setTaskType(TaskType.Decoy);

                task.setDecoyTask(decoy);
                decoyTask.add(decoy);
            }
        }
        log.info("-----诱饵任务加入数量[{}]-----",decoyTask.size());
        log.info("-----诱饵任务完成-----");
        return decoyTask;
    }

    public void selectTask(Agent agent,List<Task> taskList,List<Task> decoyList){
        Map<Task,Double> taskMap = new HashMap<>();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            double cost = agent.calCostForTask(task);

            double v;
            // 判断是否是目标任务
            if (TaskType.Target.equals(task.getTaskType())){
                Task decoyTask = task.getDecoyTask();
                double decoyCost = agent.calCostForTask(decoyTask);
                v = calMotivation(task.getTaskBid(), cost, decoyTask.getTaskId(), decoyCost);
            }else {
                v = getMaxDecoyVal(agent, task, decoyList);
            }
            taskMap.put(task,v);
        }
        // 根据契机值排序
        List<Map.Entry<Task,Double>> list = new ArrayList<>(taskMap.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<Task,Double>>() {
            @Override
            public int compare(Map.Entry<Task, Double> o1, Map.Entry<Task, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

    }

    /**
     * 获取竞争任务的最大契机值
     * @param agent
     * @param compTask
     * @param decoyList
     * @return
     */
    public double getMaxDecoyVal(Agent agent,Task compTask,List<Task> decoyList){
        double max = 0;
        for (int i = 0; i < decoyList.size(); i++) {
            Task decoyTask = decoyList.get(i);
            double compCost = agent.calCostForTask(compTask);
            double decoyCost = agent.calCostForTask(decoyTask);
            double v = calMotivation(compTask.getTaskBid(), decoyTask.getTaskBid(), compCost, decoyCost);
            max = v>max ? v:max;
        }
        return max;
    }

    /**
     * 计算契机函数的值
     * @param targetBid
     * @param decoyBid
     * @param targetCost
     * @param decoyCost
     * @return
     */
    public double calMotivation(double targetBid,
                                double decoyBid,
                                double targetCost,
                                double decoyCost){
        double res = Constant.PERFORMANCE_FACTOR * ( targetBid - decoyBid ) /(targetCost - decoyCost);
        return res;
    }

}
