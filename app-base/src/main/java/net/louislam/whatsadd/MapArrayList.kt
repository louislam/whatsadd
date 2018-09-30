package net.louislam.whatsadd

class MapArrayList<K, V> {
    private val hashMap : HashMap<K, V> = HashMap()
    private val list : ArrayList<K> = ArrayList()
    private var filteredList : ArrayList<K> = ArrayList()
    private var isUsedFilter : Boolean = false;

    init {

    }

    fun add(index: Int, key : K, element: V) {
        list.add(index, key)

        if (isUsedFilter) {
            filteredList.add(index, key)
        }

        hashMap.put(key, element)
    }


    fun get(pos : Int) : V? {
        val key : K

        if (isUsedFilter) {
            key = filteredList.get(pos)
        } else {
            key = list.get(pos)
        }

        return hashMap.get(key)
    }

    fun removeAt(index: Int): K {
        val key : K

        if (isUsedFilter) {
            key = filteredList.removeAt(index);
            list.remove(key)
        } else {
            key = list.removeAt(index);
        }

        hashMap.remove(key)
        return key;
    }

    fun remove(key : K) : V? {
        if (isUsedFilter) {
            filteredList.remove(key)
        }
        list.remove(key)
        return hashMap.remove(key);
    }

    fun getSize() : Int {
        if (isUsedFilter) {
            return filteredList.size
        } else {
            return list.size
        }
    }

    fun filter(compare: (K, V) -> Boolean) {
        isUsedFilter = true;

        filteredList = list.filter { key ->
            val element = hashMap[key]!!
            compare.invoke(key, element)
        } as ArrayList<K>
    }

    fun clearFilter() {
        isUsedFilter = false;
    }

}