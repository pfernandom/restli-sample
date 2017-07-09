package com.example.fortune.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.fortune.Fortune;
import com.linkedin.restli.server.PagingContext;
import com.linkedin.restli.server.annotations.Context;
import com.linkedin.restli.server.annotations.RestLiCollection;
import com.linkedin.restli.server.resources.CollectionResourceTemplate;

@RestLiCollection(name = "fortunes", namespace = "com.example.fortune", keyName = "fortuneId")
public class FortunesResource extends CollectionResourceTemplate<Long, Fortune>
{
  // Create trivial db for fortunes
  static Map<Long, String> fortunes = new HashMap<Long, String>();
  static {
    fortunes.put(1L, "Today is your lucky day.");
    fortunes.put(2L, "There's no time like the present.");
    fortunes.put(3L, "Don't worry, be happy.");
  }
  
  @Override
  public List<Fortune> getAll(@Context PagingContext pagingContext){
	  return fortunes.values().stream().map(s -> new Fortune().setFortune(s)).collect(Collectors.toList());
  }

  @Override
  public Fortune get(Long key)
  {
    // Retrieve the requested fortune
    String fortune = fortunes.get(key);
    if(fortune == null)
      fortune = "Your luck has run out. No fortune for id="+key;

    // return an object that represents the fortune cookie
    return new Fortune().setFortune(fortune);
  }
}