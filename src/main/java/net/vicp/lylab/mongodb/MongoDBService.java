package net.vicp.lylab.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import net.vicp.lylab.core.model.OrderBy;
import net.vicp.lylab.core.model.Page;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * MongoDBService for MongoDBOperation
 * 
 * @author liyang
 * @version 0.0.1
 */
public class MongoDBService {

	MongoCollection<Document> mongoCollection;

	public MongoDBService(MongoCollection<Document> mongoCollection) {
		this.mongoCollection = mongoCollection;
	}
	
	private List<Map<String, Object>> iterable2List(Iterable<? extends Map<String, Object>> iterable) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (iterable == null) {
			return list;
		}
		for (Map<String, Object> item : iterable) {
			Object _id = (ObjectId) item.get("_id");
			if (_id != null) {
				try {
					ObjectId objectId = (ObjectId) item.get("_id");
					item.put("_id", objectId.toHexString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			list.add(item);
		}
		return list;
	}

	// 查询除重
	public List<?> distinct(String fieldName) {
		return iterable2List(mongoCollection.distinct(fieldName, Document.class));
	}

	// 条件查询
	public List<?> find(Map<String, Object> filter) {
		return find(filter, new Page(), new OrderBy("_id", "desc"));
	}

	// 条件查询
	public List<?> find(Map<String, Object> filter, Page page, OrderBy orderBy) {
		Document tempFilter = new Document(filter);
		// FIXME 排序暂时没写
		return iterable2List(mongoCollection.find(tempFilter).sort(new Document()).skip(page.getIndex()).limit(page.getPageSize()));
	}

	// 查询统计
	public long count(Map<String, Object> filter) {
		Document tempFilter = new Document(filter);
		return mongoCollection.count(tempFilter);
	}

	// 增删改
	public void insert(Map<String, Object> document) {
		Document tempDocument = new Document(document);
		mongoCollection.insertOne(tempDocument);
	}

	public void insertMany(List<? extends Map<String, Object>> documents) {
		List<Document> list = new ArrayList<Document>();
		for (Map<String, Object> item : documents) {
			list.add(new Document(item));
		}
		mongoCollection.insertMany(list);
	}

	public UpdateResult update(String id, Map<String, Object> update) {
		Document filter = new Document();
		filter.put("_id", new ObjectId(id));
		Document tempUpdate = new Document(update);
		return mongoCollection.updateOne(filter, tempUpdate);
	}

	public UpdateResult updateMany(Map<String, Object> filter, Map<String, Object> update) {
		Document tempFilter = new Document(filter);
		Document tempUpdate = new Document(update);
		return mongoCollection.updateMany(tempFilter, tempUpdate);
	}

	public DeleteResult delete(String id) {
		Document filter = new Document();
		filter.put("_id", new ObjectId(id));
		return mongoCollection.deleteOne(filter);
	}

	public DeleteResult deleteMany(Map<String, Object> filter) {
		Document tempFilter = new Document(filter);
		return mongoCollection.deleteMany(tempFilter);
	}

	// 索引操作
	public List<?> listIndexes() {
		return iterable2List(mongoCollection.listIndexes());
	}

	public String createIndex(Map<String, Object> keys) {
		Document tempKeys = new Document(keys);
		return mongoCollection.createIndex(tempKeys);
	}

	public void dropIndex(String indexName) {
		mongoCollection.dropIndex(indexName);
	}

}
