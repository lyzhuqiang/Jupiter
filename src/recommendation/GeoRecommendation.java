package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import db.mysql.MySQLConnection;
import entity.Item;
import rpc.RpcHelper;

public class GeoRecommendation {
	public List<Item> recommendationItems(String userId, double lat, double lon){
		List<Item> recommendedItems = new ArrayList<>();
		DBConnection connection = DBConnectionFactory.getConnection();
		Set<String> favoriteItemIds = connection.getFavoriteItemIds(userId);
		Map<String, Integer> categoryMap = new HashMap<>();
		for(String id : favoriteItemIds) {
			Set<String> categories = connection.getCategories(id);
			for(String category : categories) {
				categoryMap.put(category, categoryMap.getOrDefault(category, 0) + 1);
			}
		}
		List<Entry<String, Integer>> categoryList = new ArrayList<>(categoryMap.entrySet());
		Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue());
		});
		Set<String> visitedId = new HashSet<>();
		for(Entry<String, Integer> categoryEntry : categoryList) {
			List<Item> itemList = connection.searchItems(lat, lon, categoryEntry.getKey(), 50);
			for(Item item : itemList) {
				if(visitedId.add(item.getItemId()) && !favoriteItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
				}
			}
		}
		connection.close();
		return recommendedItems;
	}
}
