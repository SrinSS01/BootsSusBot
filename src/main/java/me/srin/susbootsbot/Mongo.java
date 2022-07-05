package me.srin.susbootsbot;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class Mongo {
    static final MongoClient mongoClient = MongoClients.create("mongodb+srv://srin:ynmIyXCLcb4dY6tx@bootssusbot.uivmy.mongodb.net/sustem?retryWrites=true&w=majority");
    static final MongoDatabase sustem = mongoClient.getDatabase("sustem");
    static final MongoCollection<Document> suspoints = sustem.getCollection("suspoints");
}
