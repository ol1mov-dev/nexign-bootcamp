package com.projects;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4); // пул из 4 потоков

        for (int i = 0; i < 10; i++) {
            int taskNumber = i;
            executor.submit(() -> {
                System.out.println("Задача #" + taskNumber + " выполняется потоком " + Thread.currentThread().getName());
            });
        }

        executor.shutdown(); // важно! завершает пул после задач
    }
}