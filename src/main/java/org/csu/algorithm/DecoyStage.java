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

    public static void build(List<Task> taskList,List<Agent> agentList){
        DecoyStage decoyStage = new DecoyStage();
        // 添加诱饵任务
        List<Task> decoyTask = decoyStage.addDecoyTask(taskList);
        // 参与者选择任务
        decoyStage.selectTask(agentList,taskList,decoyTask);
        // 获胜者选择
        decoyStage.selectWinner(taskList);

        long completedSum = taskList.stream().filter(e -> e.getWinner() != null).count();
        System.out.println(completedSum);
    }

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
                        .setTaskType(TaskType.Decoy)
                        .setTaskId(Constant.TASK_NUM+i);

                task.setDecoyTask(decoy);
                decoyTask.add(decoy);
                log.info("诱饵任务信息: {}",decoy);
            }
        }
        log.info("-----诱饵任务加入数量[{}]-----",decoyTask.size());
        log.info("-----诱饵任务加入完成-----");
        return decoyTask;
    }

    /**
     * 参与者选择任务方法入口
     * @param agentList
     * @param taskList
     * @param decoyList
     */
    public void selectTask(List<Agent> agentList,List<Task> taskList,List<Task> decoyList){
        //agentList.forEach(e->selectTask(e,taskList,decoyList));
        agentList.forEach(e->selectTaskProbability(e,taskList,decoyList));
    }

    /**
     * 参与者根据契机值选择任务
     * @param agent
     * @param taskList
     * @param decoyList
     */
    public void selectTask(Agent agent,List<Task> taskList,List<Task> decoyList){
        Map<Task,Double> taskMap = new HashMap<>();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            if (null != task.getWinner()){
                continue;
            }

            double cost = agent.calCostForTask(task);
            double v;
            // 判断是否是目标任务
            if (TaskType.Target.equals(task.getTaskType())){
                Task decoyTask = task.getDecoyTask();
                double decoyCost = agent.calCostForTask(decoyTask);
                v = calMotivation(task.getTaskBid(), decoyTask.getTaskBid(), cost,decoyCost);
            }else {
                v = getMaxDecoyVal(agent, task, decoyList);
            }
            taskMap.put(task,v);
        }
        // 根据契机值排序
        List<Map.Entry<Task,Double>> list = new ArrayList<>(taskMap.entrySet());
        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        double costUpper = agent.getCostUpper();
        double curCost = 0;

        // 任务选择（按照契机值从小到大选择）
        Random random = new Random();
        for (int i = 0; i < list.size(); i++) {
            Task task = list.get(i).getKey();
            double cost = agent.calCostForTask(task);
            if (curCost+cost<costUpper){
                curCost+=cost;
                agent.getSelectedTaskSet().add(task);
                double bid = cost + (task.getTaskBid()-cost)*random.nextDouble();
                task.getSelectedAgent().put(agent,bid);
            }
        }
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

    /**
     * 概率选择任务，契机/总契机
     * @param agent
     * @param taskList
     * @param decoyList
     */
    public void selectTaskProbability(Agent agent,List<Task> taskList,List<Task> decoyList){
        Map<Task,Double> taskMap = new HashMap<>();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            if (null != task.getWinner()){
                continue;
            }

            double cost = agent.calCostForTask(task);
            double v;
            // 判断是否是目标任务
            if (TaskType.Target.equals(task.getTaskType())){
                Task decoyTask = task.getDecoyTask();
                double decoyCost = agent.calCostForTask(decoyTask);
                v = calMotivation(task.getTaskBid(), decoyTask.getTaskBid(), cost,decoyCost);
            }else {
                v = getMaxDecoyVal(agent, task, decoyList);
            }
            taskMap.put(task,v);
        }

        // 计算总契机值
        double sumV = 0;
        for (Map.Entry<Task, Double> e : taskMap.entrySet()) {
            sumV+=e.getValue();
        }

        // 更新map里的值为契机概率值
        for (Map.Entry<Task, Double> e : taskMap.entrySet()) {
            double probability = e.getValue()/sumV;
            taskMap.put(e.getKey(),probability);
        }

        // 任务选择（按照契机值概率选择）
        double costUpper = agent.getCostUpper();
        double curCost = 0;
        Random random = new Random();
        while(curCost<=costUpper && taskMap.size()>0){
            boolean flag = true;
            for (Map.Entry<Task, Double> e : taskMap.entrySet()) {
                double pro = random.nextDouble();
                Task curTask = e.getKey();
                double cost = agent.calCostForTask(curTask);
                if (pro < e.getValue() && curCost+cost<costUpper){
                    flag = false;
                    curCost+=cost;
                    agent.getSelectedTaskSet().add(curTask);
                    double bid = cost + (curTask.getTaskBid()-cost)*random.nextDouble();
                    curTask.getSelectedAgent().put(agent,bid);
                }
            }
            if (flag){
                break;
            }
        }
    }

    /**
     * 获胜者选择
     * @param taskList
     */
    public void selectWinner(List<Task> taskList){
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            Map<Agent, Double> selectedAgent = task.getSelectedAgent();
            if (selectedAgent.size() == 0) {
                continue;
            }
            // 报价降序排序
            List<Map.Entry<Agent,Double>> list = new ArrayList<>(selectedAgent.entrySet());
            Collections.sort(list, Comparator.comparing(Map.Entry::getValue));
            Agent winner = list.get(0).getKey();
            task.setWinner(winner);
            log.info("Task:{} is completed by agent[{}]",task.getTaskId(),winner.getAgentId());
        }
    }

}
