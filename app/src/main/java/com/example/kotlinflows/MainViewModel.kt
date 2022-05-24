package com.example.kotlinflows

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModel.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel : ViewModel() {
    val TAG = this.javaClass.name;
    val countDownFlow = flow<Int> {
        val initValue: Int = 10
        var currentValue: Int = initValue
        emit(initValue)


        while (currentValue > 0) {
            delay(1000L)
            currentValue--
            emit(currentValue)
        }
    }

    init {
//        mainlyUseOperators()

        //Operators
//        countOperators()
//        reduceOperators()
//        foldOperators()
//        flatteningOperations()

//        buffer, conflate, collectLatest
//        other()
//        usingBuffer()
//        usingConflate()
        usingCollectLatest()

    }

    private fun usingCollectLatest() {


        val flow55 = flow {
            delay(250L)
            emit("Appetizer")
            delay(1000L)
            emit("Main Dish")
            delay(100L)
            emit("Dessert")
        }

        //What collect latest do is as soon as it get latest collection it will start executing and
        //skip the current execution
       viewModelScope.launch {
            flow55.onEach {
                Log.e(TAG, "Flow $it is Delivered ")
            }.collectLatest{
                Log.e(TAG, "Flow $it is Been eating ")
                delay(1500L )
                Log.e(TAG, "Flow Finish eating $it  ")
            }
        }
    }
    private fun usingConflate() {


        val flow55 = flow {
            delay(250L)
            emit("Appetizer")
            delay(1000L)
            emit("Main Dish")
            delay(100L)
            emit("Dessert")
        }

        // If there is emitting flow that you can't collect yet like in our case we will Delivered Appetizer
        // then we start eating it while eating we get Main Dish and Dessert delivered. But after we finish Appetizer
        // We only execute the flow from latest emitting and drop all emitions before in our case Dessert and skip Main Dish

        //what conflate does is when get a submission from flow above like appetizer and also
        //the below collection flows didn't block the main flow execution will be sync
        // because both flow 55's flow on above and below are running in different coroutine
        viewModelScope.launch {
            flow55.onEach {
                Log.e(TAG, "Flow $it is Delivered ")
            }.conflate().collect {
                Log.e(TAG, "Flow $it is Been eating ")
                delay(1500L )
                Log.e(TAG, "Flow Finish eating $it  ")
            }
        }
    }
    private fun usingBuffer() {


        val flow55 = flow {
            delay(150L)
            emit("Appetizer")
            delay(100L)
            emit("Main Dish")
            delay(100L)
            emit("Dessert")
        }

        //the below collection flows didn't block the main flow execution will be sync
        // because both flow 55's flow on above and below are running in different coroutine
        viewModelScope.launch {
            flow55.onEach {
                Log.e(TAG, "Flow $it is Delivered ")
            }.buffer().collect {
                Log.e(TAG, "Flow $it is Been eating ")
                delay(150)
                Log.e(TAG, "Flow Finish eating $it  ")
            }
        }
    }
    private fun other() {

        val flow = flow {
            delay(150L)
            emit("Appetizer")
            delay(1000L)
            emit("Main Dish")
            delay(100L)
            emit("Dessert")
        }

        //the below collection flows block the main flow execution until it's completed
        // because both are running in a same coroutine
        viewModelScope.launch {
            flow.onEach {
                Log.e(TAG, "Flow $it is been eating ")
            }
                .collect {
                    Log.e(TAG, "Flow $it is Delivered ")
                    delay(1500)
                    Log.e(TAG, "Flow Finish eating $it  ")
                }
        }
    }
    private fun flatteningOperations() {
        //Note: Difference BT flatmap concat, merge and latest
        // Flatmap Concat finish the first task and move to second or Asynchronous
        // Flatmap Merge do all the task at the same time  Synchronise
        // Flatmap Latest only collect latest data if task 2 finished before 1 it return task 1


        //Normal list
        //{[[89,10,1,2,3,4],[5,6,7,8],[9,10,11,12]]}
        //flatten list
        //{[89,10,1,2,3,4,5,6,7,8,9,10,11,12]}

        val flow1 = (1..5).asFlow()

//        ===============Flat Map Concat================

        viewModelScope.launch {
            flow1.flatMapConcat { id ->
                //What we are going here is we are getting ids of our recipes
                //and in function getRecipieById we are getting recipie form cache by id
                //we are doing id for 5 recipies
                flow {

                    emit(getRecipeById(id))
                }
            }.collect { value ->
                Log.e(TAG, "flatteningOperations: $value")
            }
        }

//        ===============Flat Map Merge================

//        viewModelScope.launch {
//            flow1.flatMapMerge { id ->
//                //What we are going here is we are getting ids of our recipes
//                //and in function getRecipieById we are getting recipie form cache by id
//                //we are doing id for 5 recipies
//                flow {
//
//                    emit(getRecipeById(id))
//                }
//            }.collect { value ->
//                Log.e(TAG, "flatteningOperations: $value")
//            }
//        }

//        ===============Flat Map latest================
//        viewModelScope.launch {
//            flow1.flatMapLatest { id ->
//                //What we are going here is we are getting ids of our recipes
//                //and in function getRecipieById we are getting recipie form cache by id
//                //we are doing id for 5 recipies
//                flow {
//
//                    emit(getRecipeById(id))
//                }
//            }.collect { value ->
//                Log.e(TAG, "flatteningOperations: $value")
//            }
//        }
    }
    private fun foldOperators() {
        viewModelScope.launch {
            //accumulator save the value from reduce in below case the accumulator have 1 all the time
            //This is useful when save the progress instead of using global variable and use ++ for increment
            //Difference between fold and reduce is fold need an initial value
            val foldResult = countDownFlow.fold(100) { accumulator, value ->
                1
            }
            Log.e(TAG, "foldOperators: $foldResult")
        }
    }
    private fun reduceOperators() {
        viewModelScope.launch {
            //accumulator save the value from reduce in below case the accumulator have 1 all the time
            //This is useful when save the progress instead of using global variable and use ++ for increment
            val reduceResult = countDownFlow.reduce { accumulator, value ->
                1
            }
            Log.e(TAG, "reduceOperators: $reduceResult")
        }
    }
    private fun countOperators() {
        viewModelScope.launch {
            val count = countDownFlow.filter {
                it % 2 == 0
            }.map { map ->
                map * map
            }.count {
                it % 2 == 0
            }

            Log.e(TAG, "countOperators: $count ")
        }

    }
//    private fun mainlyUseOperators() {
//        viewModelScope.launch {
//            countDownFlow.filter {
//                it % 2 == 0
//            }.map { map ->
//                map * map
//
//            }.collect { data ->
//                delay(1500L)
//                Log.e(TAG, "collectFlow: $data")
//            }
//        }
//    }


    private fun getRecipeById(id: Int): Int {
        return Random(100).nextInt();

    }

}