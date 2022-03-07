package com.BarryZea.XamiNote.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.BarryZea.XamiNote.Model.Task;

import java.util.ArrayList;

public class TaskViewModel extends AndroidViewModel {
    private MutableLiveData<String> task= new MutableLiveData<>();
    private MutableLiveData<Boolean> isCancel=new MutableLiveData<>();
    private MutableLiveData<ArrayList<Task>> taskList= new MutableLiveData<>();
    public TaskViewModel(@NonNull Application application) {
        super(application);
    }
    private MutableLiveData<Task> taskEdited=new MutableLiveData<>();
    public void setTask(String task){
        this.task.postValue(task);
    }

    public LiveData<String> getTask(){
        return this.task;
    }
    public void setIsCancel(Boolean isCancel){
        this.isCancel.postValue(isCancel);
    }
    public LiveData<Boolean> getIsCancel(){
        return this.isCancel;
    }
    public void setTaskList(ArrayList<Task> taskList){
        this.taskList.postValue(taskList);
    }
    public LiveData<ArrayList<Task>> getTaskList(){
        return taskList;
    }
    public void setTaskEdited(Task taskString){
        taskEdited.postValue(taskString);
    }
    public LiveData<Task>getTaskEdited(){
        return taskEdited;
    }
    public void deleteItemInList(int itemIndex){
        if(taskList.getValue()!=null){
            ArrayList<Task> listUpdate=taskList.getValue();
            listUpdate.remove(itemIndex);
            taskList.postValue(listUpdate);
        }
    }

}
