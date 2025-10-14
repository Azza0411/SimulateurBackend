package com.demo.demo.services;

import com.demo.demo.entities.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService{
    Transaction createTransaction(Transaction transaction);
    List<Transaction> getAllTransactions();
    Optional<Transaction> getTransactionById(Integer id);
    Transaction updateTransaction(Integer id, Transaction transactionDetails);
    void deleteTransaction(Integer id);

}
