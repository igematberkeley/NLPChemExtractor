/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 *
 * @author code adapted from this website: https://www.baeldung.com/java-combinations-algorithm
 */
public class Combination {
    
    private static void helper(List<String[]> combinations, String[] arr, String[] data, int start, int end, int index) {
    if (index == data.length) {
      String[] combination = (String[])data.clone();
      combinations.add(combination);
    } else if (start <= end) {
      data[index] = arr[start];
      helper(combinations, arr, data, start + 1, end, index + 1);
      helper(combinations, arr, data, start + 1, end, index);
    } 
  }
  
  public static List<String[]> generate(String[] arr, int r) {
    List<String[]> combinations = (List)new ArrayList<>();
    helper(combinations, arr, new String[r], 0, arr.length - 1, 0);
    return combinations;
  }
  
  public static void main(String[] args) throws Exception {
    String[] arr = { "A", "B", "C" };
    List<String[]> combinations = generate(arr, 2);
    for(String[] letters:combinations){
        System.out.println(Arrays.toString(letters));
    }
  }
    
}
