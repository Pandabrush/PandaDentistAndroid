package com.pandadentist.util;

import android.text.TextUtils;

import com.pandadentist.bleconnection.utils.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Author: 张维亚
 * 创建时间：2014年6月10日 下午3:38:35 
 * 修改时间：2014年6月10日 下午3:38:35 
 * Description:资源相关操作的工具类
 **/
@SuppressWarnings("unused")
public class ZResources {

	public static int findId(String packageName, String name) {
		return findResByName(packageName, "id", name);
	}

	public static int findString(String packageName, String name) {
		return findResByName(packageName, "string", name);
	}

	public static int findDrawable(String packageName, String name) {
		return findResByName(packageName, "drawable", name);
	}

	public static int findLayout(String packageName, String name) {
		return findResByName(packageName, "layout", name);
	}

	public static int findResByName(String packageName, String className, String name) {
		try {
			Class<?> desireClass = Class.forName(packageName + ".R$" + className);
			if (desireClass != null)
				return desireClass.getField(name).getInt(desireClass);
		} catch (Exception e) {
			Logger.d("findResByName", e);
		}

		return 0;
	}

	/**
	 * 根据资源名的起始字段和一些关键字查找资源id
	 * @param clazz  ""
	 * @param rule 资源名的起始部分；比如 resName = icon_splash_360，rule = icon_splash
	 * @param more 更多的判断关键字
	 * @return 返回在查到的ID，如果未找到返回-1；
	 */
	public static int findResource(Class<?> clazz, String rule, String... more) {
		return findResource(clazz.getDeclaredFields(), rule, more);
	}

	/**
	 * 根据资源名的起始字段和一些关键字查找资源id
	 * @param fields ""
	 * @param rule 资源名的起始部分；比如 resName = icon_splash_360，rule = icon_splash
	 * @param more 更多的判断关键字
	 * @return 返回在查到的ID，如果未找到返回-1；
	 */
	public static int findResource(Field[] fields, String rule, String... more) {
		for (Field field : fields) {
			String name = field.getName().toLowerCase(Locale.getDefault());

			if (!verifyName(name, rule, more))
				continue;

			try {
				return (Integer) field.get(null);
			} catch (Exception e) {
				Logger.d("findResource", e);
			}
		}
		return -1;
	}

	/**
	 * 根据资源名的起始字段和一些关键字查找资源id列表
	 * @param clazz "
	 * @param rule 资源名的起始部分；比如 resName = icon_splash_360，rule = icon_splash
	 * @param more 更多的判断关键字
	 * @return 返回在查到的ID列表，如果未找到返回空的map；
	 */
	public static ArrayList<Integer> findResourceList(Class<?> clazz, String rule, String... more){
		return findResourceList(clazz.getDeclaredFields(), rule, more);
	}

	/**
	 * 根据资源名的起始字段和一些关键字查找资源id列表
	 * @param fields "
	 * @param rule 资源名的起始部分；比如 resName = icon_splash_360，rule = icon_splash
	 * @param more 更多的判断关键字
	 * @return 返回在查到的ID列表，如果未找到返回空的map；
	 */
	public static ArrayList<Integer> findResourceList(Field[] fields, String rule, String... more){

		HashMap<String, Integer> map = findResourceMap(fields, rule, more);
		if (map == null || map.size() <= 0)
			return new ArrayList<>(0);

		TreeMap<String, Integer> tree = new TreeMap<>(map);
		Collection<Integer> collection = tree.values();
		ArrayList<Integer> list = new ArrayList<>(collection.size());
		list.addAll(collection);
		return list;
	}

	/**
	 * 根据资源名的起始字段和一些关键字查找资源id array
	 * @param clazz "
	 * @param rule 资源名的起始部分；比如 resName = icon_splash_360，rule = icon_splash
	 * @param more 更多的判断关键字
	 * @return 返回在查到的ID列表，如果未找到返回空的map；
	 */
	public static Integer[] findResourceArray(Class<?> clazz, String rule, String... more){
		return findResourceArray(clazz.getDeclaredFields(), rule, more);
	}

	/**
	 * 根据资源名的起始字段和一些关键字查找资源id array
	 * @param fields "
	 * @param rule 资源名的起始部分；比如 resName = icon_splash_360，rule = icon_splash
	 * @param more 更多的判断关键字
	 * @return 返回在查到的ID列表，如果未找到返回空的map；
	 */
	public static Integer[] findResourceArray(Field[] fields, String rule, String... more){
		HashMap<String, Integer> map = findResourceMap(fields, rule, more);
		if (map == null || map.size() <= 0)
			return new Integer[0];
		
		TreeMap<String, Integer> tree = new TreeMap<>(map);
		Collection<Integer> collection = tree.values();
		return collection.toArray(new Integer[collection.size()]);
	}

	/**
	 * 根据资源名的起始字段和一些关键字查找资源id列表
	 * @param clazz "
	 * @param rule 资源名的起始部分；比如 resName = icon_splash_360，rule = icon_splash
	 * @param more 更多的判断关键字
	 * @return 返回在查到的ID列表，如果未找到返回空的map；
	 */
	public static HashMap<String, Integer> findResourceMap(Class<?> clazz, String rule, String... more){
		return findResourceMap(clazz.getDeclaredFields(), rule, more);
	}

	/**
	 * 根据资源名的起始字段和一些关键字查找资源id列表
	 * @param fields "
	 * @param rule 资源名的起始部分；比如 resName = icon_splash_360，rule = icon_splash
	 * @param more 更多的判断关键字
	 * @return 返回在查到的ID列表，如果未找到返回空的map；
	 */
	public static HashMap<String, Integer> findResourceMap(Field[] fields, String rule, String... more) {
		HashMap<String, Integer> hashMap = new HashMap<>();
		for (Field field : fields) {
			String name = field.getName().toLowerCase(Locale.getDefault());

			if (!verifyName(name, rule, more))
				continue;

			try {
				hashMap.put(name, (Integer) field.get(null));
			} catch (Exception e) {
				Logger.d("findResourceMap", e);
			}
		}
		return hashMap;
	}

	/**
	 * @param name ID 对应的name
	 * @param rule 起始字段
	 * @param more 更多的判断关键字
	 * @return true 符合条件的ID，false 不符合条件的ID
	 */
	private static boolean verifyName(String name, String rule, String... more) {
		if (TextUtils.isEmpty(name)) 
			return false;

		if (!name.startsWith(rule))
			return false;

		if (more == null || more.length <= 0)
			return true;

		for (String string : more) {
			if (!name.contains(string))
				return false;
		}

		return true;
	}
}