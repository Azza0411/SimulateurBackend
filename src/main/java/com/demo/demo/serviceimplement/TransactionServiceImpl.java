package com.demo.demo.serviceimplement;

import com.demo.demo.entities.Transaction;
import com.demo.demo.repository.TransactionRepository;
import com.demo.demo.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service

public class TransactionServiceImpl  implements TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public Optional<Transaction> getTransactionById(Integer id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Transaction updateTransaction(Integer id, Transaction transactionDetails) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setTypeT(transactionDetails.getTypeT());
        transaction.setQuantiteActif(transactionDetails.getQuantiteActif());
        transaction.setPrixTransaction(transactionDetails.getPrixTransaction());
        transaction.setTypeTransaction(transactionDetails.getTypeTransaction());
        transaction.setStatusTransaction(transactionDetails.getStatusTransaction());
        transaction.setRisque(transactionDetails.getRisque());
        transaction.setPrixCible(transactionDetails.getPrixCible());
        transaction.setFrais(transactionDetails.getFrais());
        transaction.setGainPerte(transactionDetails.getGainPerte());
        transaction.setDateCreation(transactionDetails.getDateCreation());
        transaction.setDateExecution(transactionDetails.getDateExecution());
        transaction.setModeValidation(transactionDetails.getModeValidation());
        transaction.setSimulation(transactionDetails.getSimulation());
        transaction.setActifs(transactionDetails.getActifs());
        return transactionRepository.save(transaction);
    }
    @Override
    public void deleteTransaction(Integer id) {
        transactionRepository.deleteById(id);

    }
}
