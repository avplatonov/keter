package ru.avplatonov.keter.keterbackend.initialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GraphInitialize {
    private String nameGraph;
    private List<String> tagsGraph;
    private String descriptionTag;

    GraphInitialize(){

    }

    GraphInitialize(String nameGraph, List<String> tagsGraph, String descriptionTag){
        this.nameGraph = nameGraph;
        this.tagsGraph = tagsGraph;
        this.descriptionTag = descriptionTag;
    }

    public void setDescriptionTag(String descriptionTag) {
        this.descriptionTag = descriptionTag;
    }

    public void setTagGraph(List<String> tagGraph) {
        this.tagsGraph = tagGraph;
    }

    public void setNameGraph(String nameGraph) {
        this.nameGraph = nameGraph;
    }

    public String getDescriptionTag() {
        return descriptionTag;
    }

    public List<String> getTagGraph() {
        return tagsGraph;
    }

    public String getNameGraph() {
        return nameGraph;
    }

    @Override
    public String toString()
    {
        String outGraph = "[NameNodesToGraph: { name: " + getNameGraph() + " description: " + getDescriptionTag() + " tags: [" ;
        for (String tag : getTagGraph()
             ) {
            outGraph+=tag + ", ";
        }
        return outGraph + "]}]";
    }

    public static ObjectMapper getSmallList(){
        List<String> tagList1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tagList1.add("tag " + i + new Date());
        }
        GraphInitialize graphInitialize1 = new GraphInitialize("one", tagList1, "ololo");

        List<String> tagList2 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tagList2.add("nontag " + i + new Date());
        }
        GraphInitialize graphInitialize2 = new GraphInitialize("one", tagList2, "Печальный Демон, дух изгнанья, Летал над грешною землей, И лучших дней воспоминанья Пред ним теснилися толпой");

        List<String> tagList3 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tagList3.add("tag " + i + new Date());
        }
        GraphInitialize graphInitialize3 = new GraphInitialize("one", tagList3, "HTTP Status 404 - /product");

        List<String> tagList = new ArrayList<>();
        tagList.add(graphInitialize1.toString() + "\n");
        tagList.add(graphInitialize2.toString() + "\n");
        tagList.add(graphInitialize3.toString() + "\n");

        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValueAsString(graphInitialize1);
            mapper.writeValueAsString(graphInitialize2);
            mapper.writeValueAsString(graphInitialize3);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        return mapper;
    }
}
