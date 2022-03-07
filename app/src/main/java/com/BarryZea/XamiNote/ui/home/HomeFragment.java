package com.BarryZea.XamiNote.ui.home;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.BarryZea.XamiNote.Adapter.NoteAdapterRecyclerView;
import com.BarryZea.XamiNote.DataBase.MyDataBaseHelper;
import com.BarryZea.XamiNote.DialogsCustom.OrderListDialogFragment;
import com.BarryZea.XamiNote.Events.DeleteNoteEvent;
import com.BarryZea.XamiNote.Events.EditNoteEvent;

import com.BarryZea.XamiNote.Interfaces.CallbackDismiss;
import com.BarryZea.XamiNote.Interfaces.ClickEventInterface;
import com.BarryZea.XamiNote.Interfaces.NoteEventInterface;
import com.BarryZea.XamiNote.Model.Note;
import com.BarryZea.XamiNote.Model.Task;
import com.BarryZea.XamiNote.MyTaskDetailActivity;
import com.BarryZea.XamiNote.NoteDetailActivity;
import com.BarryZea.XamiNote.NotesListActivity;
import com.BarryZea.XamiNote.R;
import com.BarryZea.XamiNote.Repository.DatabaseRepository;
import com.BarryZea.XamiNote.Utils.ActionsInControls;
import com.BarryZea.XamiNote.Utils.Constants;
import com.BarryZea.XamiNote.Utils.Preferences;

import com.BarryZea.XamiNote.databinding.FragmentHomeBinding;
import com.BarryZea.XamiNote.ui.calendar.CalendarFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public  class HomeFragment extends Fragment  implements NoteEventInterface , CallbackDismiss {
    private static final int NOTE_ACTIVITY_REQUEST = 1;
    private static final int TASK_ACTIVITY_REQUEST = 2;
    private static final int STICKY_STYLE = 1;
    private static final int NOTE_STYLE = 2;
    private static final int ORDER_FOR_COLOR = 1;
    private static final int ORDERED_LAST_REGISTER = 2;
    private static final int ORDERED_LAST_UPDATE = 3;
    private static final int ORDERED_ALPHABETICAL = 4;
    public static SearchView searchView;

    public static HomeFragment homeFragment;
    public  HomeFragment(){homeFragment=this;}
    private LinearLayoutManager linearLayoutManager;
    public static NoteAdapterRecyclerView noteAdapter;
    private ArrayList<Note>listNotes=new ArrayList<>();
    private ArrayList<Task> taskList= new ArrayList<>();

    public static OrderListDialogFragment dialogFragment;
    private MyDataBaseHelper dbInstance;
    private  int stylePosition=0;

    private static final int my_request=1;

    private int numColumnsGridMng=1;
    private FloatingActionButton mfb, mfb1, mfb2;
    private Menu menu;
    private boolean prefTheme;
    private HomeFragmentViewModel viewModel;


    private FragmentHomeBinding bind;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        bind=FragmentHomeBinding.inflate(getLayoutInflater());
        viewModel=new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);



        prefTheme=Preferences.getThemePref();
        dbInstance=DatabaseRepository.getInstance(getActivity());

        setUpTheme();
        getSharedPreferences();

        setHasOptionsMenu(true);
        dialogFragment=  new OrderListDialogFragment(getActivity());


        setUpListenerFab();

        setUpScrollListenerRecyclerView();
        setUpOptionOrderListener();
        getListNotesRecyclerView();


        return bind.getRoot();


    }
    private void checkIfListIsEmpty(){
        if(noteAdapter.getItemCount()>0){
            bind.layoutIfRecyclerViewEmpty.setVisibility(View.GONE);
        }
        else{bind.layoutIfRecyclerViewEmpty.setVisibility(View.VISIBLE);}
    }
    private void getSharedPreferences(){

        stylePosition= Preferences.getStyleNote();

    }
    private void setUpTheme() {
        if (prefTheme) {
            getActivity().setTheme(R.style.AppThemeDark);
        } else {
            getActivity().setTheme(R.style.AppTheme);
        }
        if (prefTheme) {

            getActivity().getWindow().setStatusBarColor(Color.parseColor("#121212"));

            ((NotesListActivity) getActivity()).changeColorToolbar("#1F1B24");

        } else {
            String colorDark = "#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark) & 0x00ffffff);
            String colorPrimary = "#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.colorPrimary) & 0x00ffffff);
            ((NotesListActivity) getActivity()).changeColorToolbar(colorPrimary);
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }
    private void setUpListenerFab(){
        mfb=((NotesListActivity)getActivity()).getFloatingActionButton();
        mfb1=((NotesListActivity)getActivity()).getFloatingActionButtonNote();
        mfb2=((NotesListActivity)getActivity()).getFloatingActionButtonList();

    }
    private void showDialogOrderOptions(){

        dialogFragment.setRetainInstance(true);
        dialogFragment.setTargetFragment(HomeFragment.this,my_request);
        dialogFragment.show(HomeFragment.this.getParentFragmentManager(),"Try");
    }
    private void setUpScrollListenerRecyclerView(){
        bind.listNotesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if(dy>0 && mfb.getVisibility()==View.VISIBLE)
                {
                    mfb.hide();
                }

                else if(dy <0 && mfb.getVisibility()!=View.VISIBLE){
                    mfb.show();
                }
                else
                {
                    mfb.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void setUpOptionOrderListener(){

        viewModel.getOptionColorFilter().observe(getViewLifecycleOwner(), (value)-> {
            Preferences.setOrderColorNotes(value);
            getListNotesRecyclerView();
        });
        viewModel.getOptionOrder().observe(getViewLifecycleOwner(), value -> actionsForOrderList(value));
    }


    public  void getListNotesRecyclerView()
    {

        numColumnsGridMng= ActionsInControls.calculateColumnsForCardViews(getContext(),180);


        listNotes= dbInstance.getNotesWithOrder(Preferences.getOrderNotes(),Preferences.getOrderColorNotes());
        taskList=dbInstance.getAllTask();

        linearLayoutManager= new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        int resId=R.anim.layout_animation_slide_right;
        LayoutAnimationController animation= AnimationUtils.loadLayoutAnimation(getContext(),resId);
        bind.listNotesRecycler.setLayoutAnimation(animation);
        //********************************************************************************
        if(stylePosition==1)
        {


            bind.listNotesRecycler.setLayoutManager(linearLayoutManager);
            noteAdapter=new NoteAdapterRecyclerView(bind.listNotesRecycler, R.layout.card_view_sticky_style, getActivity(), stylePosition,  new ClickEventInterface() {
                @Override
                public void onClick(Note note) {

                    if (note.getIdTypeDocument() == 1) {

                        Intent intentDetailNote = new Intent(getActivity(), NoteDetailActivity.class);
                        intentDetailNote.putExtra(Constants.NOTE_EXTRA_KEY,note);
                        startActivityForResult(intentDetailNote, NOTE_ACTIVITY_REQUEST);
                        if(note.getOriginOfOpen() ==1) CalendarFragment.calendarFragment.dismissDialogListReminder();

                    } else {


                        Intent taskIntent = new Intent(getActivity(), MyTaskDetailActivity.class);
                        taskIntent.putExtra(Constants.NOTE_EXTRA_KEY,note);

                        startActivityForResult(taskIntent, TASK_ACTIVITY_REQUEST);
                        if(note.getOriginOfOpen() ==1) CalendarFragment.calendarFragment.dismissDialogListReminder();
                    }
                }
            });

        }
        else
        {

            bind.listNotesRecycler.setLayoutManager(new GridLayoutManager(getContext(), numColumnsGridMng));
            noteAdapter=new NoteAdapterRecyclerView(bind.listNotesRecycler, R.layout.card_view_notes_style, getActivity(), stylePosition,  new ClickEventInterface() {
                @Override
                public void onClick(Note note) {


                    if (note.getIdTypeDocument() == 1) {

                        Intent intentDetailNote = new Intent(getActivity(), NoteDetailActivity.class);
                        intentDetailNote.putExtra(Constants.NOTE_EXTRA_KEY,note);
                        startActivityForResult(intentDetailNote, NOTE_ACTIVITY_REQUEST);
                        if(note.getOriginOfOpen() ==1) CalendarFragment.calendarFragment.dismissDialogListReminder();

                    } else {


                        Intent taskIntent = new Intent(getActivity(), MyTaskDetailActivity.class);
                        taskIntent.putExtra(Constants.NOTE_EXTRA_KEY,note);

                        startActivityForResult(taskIntent, TASK_ACTIVITY_REQUEST);
                        if(note.getOriginOfOpen() ==1) CalendarFragment.calendarFragment.dismissDialogListReminder();
                    }
                }
            });
        }

        bind.listNotesRecycler.setHasFixedSize(true);
        bind.listNotesRecycler.setItemViewCacheSize(20);
        bind.listNotesRecycler.setAdapter(noteAdapter);
        noteAdapter.addAllNotes(listNotes);
        noteAdapter.addAllTask(taskList);
        checkIfListIsEmpty();

    }
    private void actionsForOrderList(String option){
        switch (option) {
            case Constants.STICKY_STYLE:

                Preferences.setStyleNotes(STICKY_STYLE);

                getSharedPreferences();
                getListNotesRecyclerView();
                break;
            case Constants.NOTE_STYLE:

                Preferences.setStyleNotes(NOTE_STYLE);

                getSharedPreferences();
                getListNotesRecyclerView();
                break;
            case Constants.ORDER_FOR_COLOR:


                Preferences.setOrderedNotes(ORDER_FOR_COLOR);

                getListNotesRecyclerView();


                break;
            case Constants.ORDERED_LAST_REGISTER:


                Preferences.setOrderedNotes(ORDERED_LAST_REGISTER);
                getListNotesRecyclerView();

                break;
            case Constants.ORDERED_LAST_UPDATE:

                Preferences.setOrderedNotes(ORDERED_LAST_UPDATE);

                getListNotesRecyclerView();

                break;
            case Constants.ORDERED_ALPHABETICAL:

                Preferences.setOrderedNotes(ORDERED_ALPHABETICAL);

                getListNotesRecyclerView();

                break;
        }


    }

    void showMessageIfRecyclerViewIsEmpty(int numItems)
    {
        if(numItems<=0 )
        {   if(prefTheme)
            {

                bind.imageTaskEmpty.setColorFilter(getResources().getColor(android.R.color.white));
                bind.imageNoteEmpty.setColorFilter(getResources().getColor(android.R.color.white));
                bind.tvNoData.setTextColor(getResources().getColor(R.color.colorAccentDarkCustom));
                bind.tvNoData2.setTextColor(getResources().getColor(R.color.colorAccentDarkCustom));
            }
            bind.layoutIfRecyclerViewEmpty.setVisibility(View.VISIBLE);
        }
        else
        {
            bind.layoutIfRecyclerViewEmpty.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        mfb.show();
        super.onResume();

        bind.listNotesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if(dy>0 && mfb.getVisibility()==View.VISIBLE)
                {
                    mfb.hide();
                }

                else if(dy <0 && mfb.getVisibility()!=View.VISIBLE){
                    mfb.show();
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        NotesListActivity.getInstance().clickStateFB();

        if(Preferences.getThemePref())
        {
            getActivity().setTheme(R.style.AppThemeDark);

        }

        mfb.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_action_plus));


    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onStop() {
       EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu ,MenuInflater inflate)
    {
        this.menu=menu;
        getActivity().getMenuInflater().inflate(R.menu.main,
                menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager)getActivity().
                getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new
                                                  SearchView.OnQueryTextListener() {
                                                      @Override
                                                      public boolean onQueryTextSubmit(String query) {
                                                          // filter recycler view when query submitted

                                                              noteAdapter.getFilter().filter(query);

                                                          return false;
                                                      }

                                                      @Override
                                                      public boolean onQueryTextChange(String query) {
                                                          // filter recycler view when text is changed
                                                          noteAdapter.getFilter().filter(query);
                                                          return false;
                                                      }
                                                  });
    }

   //************************************************************


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_filter:
                showDialogOrderOptions();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @Subscribe(sticky = true)
    public void onNoteEditEvent(EditNoteEvent event) {
        if(event!=null) {
            noteAdapter.addNote(event.getNote());

            noteAdapter.addAllTask(event.getTaskList());
            EventBus.getDefault().removeStickyEvent(EditNoteEvent.class);
            showMessageIfRecyclerViewIsEmpty(noteAdapter.getItemCount());
        }
    }

    @Override
    @Subscribe(sticky = true)
    public void onDeletedNoteEvent(DeleteNoteEvent event) {
        noteAdapter.removeNote(event.getNote());
        noteAdapter.removeTask(event.getTaskDeleted());
        EventBus.getDefault().removeStickyEvent(DeleteNoteEvent.class);
        showMessageIfRecyclerViewIsEmpty(noteAdapter.getItemCount());
        checkIfListIsEmpty();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.getOptionColorFilter().removeObservers(this);
        viewModel.getOptionOrder().removeObservers(this);
        EventBus.getDefault().unregister(this);
        checkIfListIsEmpty();
    }

    @Override
    public void dismissDialog() {
        checkIfListIsEmpty();
    }


    //*******************************************************
}
