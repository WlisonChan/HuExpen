package org.csu.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.csu.entity.Agent;
import org.csu.entity.Task;
import org.csu.type.TaskType;
import org.csu.mcs.Constant;
import org.csu.mcs.Main;

import java.util.*;

@Slf4j
public class DecoyStage {

    // 诱饵任务与目标任务的质量转化比
    public static final double DECOY_QUALITY_TRANSFER = 0.99;

    // 诱饵任务与目标任务的报价转化比
    public static final double DECOY_BID_TRANSFER = 0.93;

    // 提交任务质量的随机值
    // 低于此值则未提交任务
    public static final double RANDOM_QUALITY_UNCOMMIT = 0.10;

    // 低质量
    public static final double RANDOM_QUALITY_LOW = 0.40;

    // 低于目标质量高于平均质量
    public static final double RANDOM_QUALITY_MIDDLE = 0.70;

    // 高质量
    public static final double RANDOM_QUALITY_HIGH = 1.00;

    /**
     * 算法入口
     *
     * @param taskList
     * @param agentList
     */
    public static void build(List<Task> taskList, List<Agent> agentList) {
        DecoyStage decoyStage = new DecoyStage();
        // 添加诱饵任务
        List<Task> decoyTask = decoyStage.addDecoyTask(taskList);
        // 参与者选择任务
        decoyStage.selectTask(agentList, taskList, decoyTask);
        // 获胜者选择
        decoyStage.selectWinner(taskList);
        // 报酬支付
        decoyStage.payForAgent(agentList);
        // 信息打印
        decoyStage.printTaskInfo(taskList);
        decoyStage.printAgentInfo(agentList);
    }

    /**
     * 添加诱饵任务
     *
     * @param taskList
     */
    public List<Task> addDecoyTask(List<Task> taskList) {
        log.info("-----诱饵任务加入-----");
        List<Task> decoyTask = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            if (TaskType.Target.equals(task.getTaskType())) {
                Task decoy = new Task();
                decoy.setTaskQuality(task.getTaskQuality() * DECOY_QUALITY_TRANSFER)
                        .setTaskBid(task.getTaskBid() * DECOY_BID_TRANSFER)
                        .setTaskType(TaskType.Decoy)
                        .setTaskId(Constant.TASK_NUM + i);

                task.setDecoyTask(decoy);
                decoyTask.add(decoy);
                log.info("诱饵任务信息: {}", decoy);
            }
        }
        log.info("-----诱饵任务加入数量[{}]-----", decoyTask.size());
        log.info("-----诱饵任务加入完成-----");
        return decoyTask;
    }

    /**
     * 参与者选择任务方法入口
     *
     * @param agentList
     * @param taskList
     * @param decoyList
     */
    public void selectTask(List<Agent> agentList, List<Task> taskList, List<Task> decoyList) {
        //agentList.forEach(e->selectTask(e,taskList,decoyList));
        agentList.forEach(e -> selectTaskProbability(e, taskList, decoyList));
    }

    /**
     * 参与者根据契机值选择任务
     *
     * @param agent
     * @param taskList
     * @param decoyList
     */
    public void selectTask(Agent agent, List<Task> taskList, List<Task> decoyList) {
        Map<Task, Double> taskMap = new HashMap<>();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            if (null != task.getWinner()) {
                continue;
            }

            double cost = agent.calCostForTask(task);
            double v;
            // 判断是否是目标任务
            if (TaskType.Target.equals(task.getTaskType())) {
                Task decoyTask = task.getDecoyTask();
                double decoyCost = agent.calCostForTask(decoyTask);
                v = calMotivation(task.getTaskBid(), decoyTask.getTaskBid(), cost, decoyCost);
            } else {
                v = getMaxDecoyVal(agent, task, decoyList);
            }
            taskMap.put(task, v);
        }
        // 根据契机值排序
        List<Map.Entry<Task, Double>> list = new ArrayList<>(taskMap.entrySet());
        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        double costUpper = agent.getCostUpper();
        double curCost = 0;

        // 任务选择（按照契机值从小到大选择）
        Random random = new Random();
        for (int i = 0; i < list.size(); i++) {
            Task task = list.get(i).getKey();
            double cost = agent.calCostForTask(task);
            if (curCost + cost < costUpper) {
                curCost += cost;
                double bid = cost + (task.getTaskBid() - cost) * random.nextDouble();
                Object[] obj = new Object[2];
                obj[0] = task;
                obj[1] = bid;
                agent.getSelectedTaskSet().add(obj);
                task.getSelectedAgent().put(agent, bid);
            }
        }
    }

    /**
     * 获取竞争任务的最大契机值
     *
     * @param agent
     * @param compTask
     * @param decoyList
     * @return
     */
    public double getMaxDecoyVal(Agent agent, Task compTask, List<Task> decoyList) {
        double max = 0;
        for (int i = 0; i < decoyList.size(); i++) {
            Task decoyTask = decoyList.get(i);
            double compCost = agent.calCostForTask(compTask);
            double decoyCost = agent.calCostForTask(decoyTask);
            double v = calMotivation(compTask.getTaskBid(), decoyTask.getTaskBid(), compCost, decoyCost);
            max = v > max ? v : max;
        }
        return max;
    }

    /**
     * 计算契机函数的值
     *
     * @param targetBid
     * @param decoyBid
     * @param targetCost
     * @param decoyCost
     * @return
     */
    public double calMotivation(double targetBid,
                                double decoyBid,
                                double targetCost,
                                double decoyCost) {
        double res = Constant.PERFORMANCE_FACTOR * (targetBid - decoyBid) / (targetCost - decoyCost);
        return res;
    }

    /**
     * 概率选择任务，契机/总契机
     *
     * @param agent
     * @param taskList
     * @param decoyList
     */
    public void selectTaskProbability(Agent agent, List<Task> taskList, List<Task> decoyList) {
        Map<Task, Double> taskMap = new HashMap<>();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            if (null != task.getWinner()) {
                continue;
            }

            double cost = agent.calCostForTask(task);
            double v;
            // 判断是否是目标任务
            if (TaskType.Target.equals(task.getTaskType())) {
                Task decoyTask = task.getDecoyTask();
                double decoyCost = agent.calCostForTask(decoyTask);
                v = calMotivation(task.getTaskBid(), decoyTask.getTaskBid(), cost, decoyCost);
            } else {
                v = getMaxDecoyVal(agent, task, decoyList);
            }
            taskMap.put(task, v);
        }

        // 计算总契机值
        double sumV = 0;
        for (Map.Entry<Task, Double> e : taskMap.entrySet()) {
            sumV += e.getValue();
        }

        // 计算map里的契机概率值
        List<Object[]> ele = new ArrayList<>();
        double floor = 0.0;
        double upper = 0.0;
        for (Map.Entry<Task, Double> e : taskMap.entrySet()) {
            double probability = e.getValue() / sumV;
            upper += probability;
            Object[] temp = new Object[3];
            temp[0] = e.getKey();
            temp[1] = floor;
            temp[2] = upper;
            ele.add(temp);
            floor = upper;
        }

        // 任务选择（按照契机值概率选择）
        double costUpper = agent.getCostUpper();
        double curCost = 0;
        Random random = new Random();
        List<Integer> selected = new ArrayList<>();
        while (curCost <= costUpper && selected.size() != ele.size()) {
            double pro = random.nextDouble();
            boolean flag = true;
            for (int i = 0; i < ele.size(); i++) {
                // 任务被选择则跳过
                if (selected.contains(i)) {
                    flag = false;
                    continue;
                }
                Object[] e = ele.get(i);
                Task task = (Task) e[0];
                double cost = agent.calCostForTask(task);
                if (curCost + cost > costUpper) {
                    flag = false;
                    selected.add(i);
                    continue;
                }
                double f = (Double) e[1];
                double u = (Double) e[2];
                // 在概率范围内且任务未被选择
                if (pro > f && pro < u) {
                    flag = false;
                    selected.add(i);
                    curCost += cost;
                    // 参与者对任务报价
                    double bid = cost + (task.getTaskBid() - cost) * random.nextDouble();
                    Object[] obj = new Object[2];
                    obj[0] = task;
                    obj[1] = bid;
                    agent.getSelectedTaskSet().add(obj);
                    task.getSelectedAgent().put(agent, bid);
                }
            }
            if (flag) {
                break;
            }
        }
    }

    /**
     * 获胜者选择
     *
     * @param taskList
     */
    public void selectWinner(List<Task> taskList) {
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            Map<Agent, Double> selectedAgent = task.getSelectedAgent();
            if (selectedAgent.size() == 0) {
                continue;
            }

            // 报价降序排序
            List<Map.Entry<Agent, Double>> list = new ArrayList<>(selectedAgent.entrySet());
            Collections.sort(list, Comparator.comparing(Map.Entry::getValue));
            Agent winner = list.get(0).getKey();
            task.setWinner(winner);

            Object[] obj = new Object[3];
            obj[0] = task;
            obj[1] = list.get(0).getValue();

            winner.getCompletedTask().add(obj);
            log.info("Winner selection - Task:{} is completed by agent[{}]", task.getTaskId(), winner.getAgentId());
        }
    }

    /**
     * 任务完成情况信息打印
     *
     * @param taskList
     */
    public void printTaskInfo(List<Task> taskList) {
        log.info("-----任务完成情况-----");
        long completedTargetCount = taskList.stream()
                .filter(e -> TaskType.Target.equals(e.getTaskType()) && e.getWinner() != null)
                .count();
        long completedCompCount = taskList.stream()
                .filter(e -> TaskType.Compete.equals(e.getTaskType()) && e.getWinner() != null)
                .count();
        log.info("任务完成总数量：[{}]", completedTargetCount + completedCompCount);
        log.info("目标类任务完成数量：[{}]", completedTargetCount);
        log.info("竞争类任务完成数量：[{}]", completedCompCount);

    }

    /**
     * 参与者信息打印
     *
     * @param agentList
     */
    public void printAgentInfo(List<Agent> agentList) {
        double winnerSize = 0;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            List<Object[]> taskSet = agent.getSelectedTaskSet();
            if (agent.getCompletedTask().size() > 0) {
                winnerSize++;
            }
            long count = 0;
            for (int j = 0; j < taskSet.size(); j++) {
                Object[] obj = taskSet.get(j);
                Task task = (Task) obj[0];
                if (TaskType.Compete.equals(task.getTaskType())){
                    count++;
                }
            }
            if (count == taskSet.size()) {
                String s = "参与者id:" + agent.getAgentId() + "\t 竞争任务数量：" + count + "\n";
                str.append(s);
            }
        }
        log.info("获胜者人数:[{}]", winnerSize);
        log.info("-----仅选择竞争的任务信息如下-----");
        String[] split = str.toString().split("\n");
        for (int i = 0; i < split.length; i++) {
            log.info("{}", split[i]);
        }
        log.info("-----报酬支付信息-----");
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            double sum = agent.getPaySet().stream().mapToDouble(Double::doubleValue).sum();
            log.info("agent:[{}] get pay [{}]",agent.getAgentId(),String.format("%.2f",sum));
        }
    }

    /**
     * 报酬支付
     *
     * @param agentList
     */
    public void payForAgent(List<Agent> agentList) {
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            List<Object[]> taskSet = agent.getCompletedTask();
            double pay = 0;
            Random random = new Random();
            for (int j = 0; j < taskSet.size(); j++) {
                Object[] obj = taskSet.get(j);
                Task task = (Task) obj[0];
                double bidVal = (Double)obj[1];
                double x = random.nextDouble();
                if (x < RANDOM_QUALITY_UNCOMMIT) {
                    obj[2] = 0D;
                    continue;
                }
                if (TaskType.Target.equals(task.getTaskType())) {
                    if (x < RANDOM_QUALITY_LOW) {
                        double actualCost = Main.avgQ * random.nextDouble();
                        obj[2] = actualCost;
                        continue;
                    } else if (x < RANDOM_QUALITY_MIDDLE) {
                        double actQ = Main.avgQ + (task.getTaskQuality() - Main.avgQ) * random.nextDouble();
                        double curPay = bidVal * actQ / task.getTaskQuality();
                        obj[2] = agent.calCostForTask(actQ);
                        pay += curPay;
                    } else {
                        obj[2] = agent.calCostForTask(task);
                        pay += bidVal;
                    }
                } else {
                    obj[2] = agent.calCostForTask(task);
                    pay += bidVal;
                }
            }
            agent.getPaySet().add(pay);
        }
    }

}
