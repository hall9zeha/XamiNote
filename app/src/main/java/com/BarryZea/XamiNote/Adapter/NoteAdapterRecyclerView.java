package com.BarryZea.XamiNote.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.BarryZea.XamiNote.DataBase.MyDataBaseHelper;
import com.BarryZea.XamiNote.Interfaces.CallbackDismiss;
import com.BarryZea.XamiNote.Interfaces.ClickEventInterface;
import com.BarryZea.XamiNote.MyApp;
import com.BarryZea.XamiNote.NotesListActivity;
import com.BarryZea.XamiNote.Repository.DatabaseRepository;
import com.BarryZea.XamiNote.Utils.ActionsInControls;
import com.BarryZea.XamiNote.Model.Note;
import com.BarryZea.XamiNote.Model.Task;
import com.BarryZea.XamiNote.R;
import com.BarryZea.XamiNote.Utils.CloseNotificationHelper;
import com.BarryZea.XamiNote.Utils.Constants;


import com.BarryZea.XamiNote.databinding.OptionsColorsPopupBinding;
import com.BarryZea.XamiNote.ui.home.HomeFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteAdapterRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable{

    private static final int NOTE = 1;
    private static final int TASK = 2;
    private static final int YELLOW = 1;
    private static final int ORANGE = 2;
    private static final int GREEN = 3;
    private static final int BLUE = 4;
    private static final int VIOLET = 5;
    private static final int PINK = 6;
    public ArrayList<Note> listNotes=new ArrayList<>();
    private ArrayList<Note> listNotesFiltered=new ArrayList<>();

    public ArrayList<Task> taskList= new ArrayList<>();
    private int resource;
    private   Activity activity;
    private int stylePosition;


    private ClickEventInterface event;
    private MyDataBaseHelper dbInstance;
    OptionsColorsPopupBinding bindDialogColor;
    private RecyclerView rvListNotes;
    private ActionMode actionMode;
    private boolean action_mode=false;
    private SparseBooleanArray selectedItems;
    private static int currentSelectedIndex = -1;
    boolean stateMenu=true;
    private ArrayList<Note> listNotesForActionMode= new ArrayList<>();
    private CallbackDismiss callbackDismiss=(CallbackDismiss) HomeFragment.homeFragment;


    /*
    * No usaremos la carga de las listas desde el constrauctor ya que el filterable no cogía las listas llenas, al parecer las tomaba sin inicializar y vacías
    * Ahora cargamos las listas con dos métodos addAllNotes y addAllTask despues de instanciar el adaptador
    * funciona correctamente para nuestros propósitos
    * */
    public NoteAdapterRecyclerView(RecyclerView recyclerView, int resource, Activity activity, int stylePosition,final ClickEventInterface event)
    {

        this.resource=resource;
        this.activity=activity;
        this.stylePosition=stylePosition;

        this.rvListNotes=recyclerView;

        this.event=event;
        dbInstance = DatabaseRepository.getInstance(MyApp.getContext());
        this.selectedItems= new SparseBooleanArray();



    }

    public void clearAllList(){
        listNotesFiltered.clear();
        taskList.clear();
        notifyDataSetChanged();
    }
    //******************************************************
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflando el menu recycler realizando cambios en masa
      View view= LayoutInflater.from(parent.getContext()).inflate(resource,parent,false);
            return new NoteViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position ) {
         Note notes = listNotesFiltered.get(position);
        ((NoteViewHolder) holder).itemView.setActivated(selectedItems.get(position, false));


        if(holder instanceof  NoteViewHolder) {


              ActionsInControls.setPrevContentNote(notes,taskList, holder.itemView, stylePosition);
              //mostrando aspas cuando es una lista de tareas
              //taslIndicator -> es una aspa que indica que es una lista de tareas
              //tsIndicator2 -> es una candado indica si la tarea o nota esta bloqueada con contraseña
              //***********************************************************************

              int tasksRealized = 0, totalTasks = 0;
              if (notes.getIdTypeDocument() == TASK) {
                  for (Task ts : taskList) {
                      if (ts.getIdNoteReference() == notes.getIdNota()) {
                          if (ts.getStateTask() == 2) {

                              tasksRealized++;
                          }
                          totalTasks++;
                      }

                  }
                  if (tasksRealized == totalTasks && tasksRealized > 0) {
                      switch(stylePosition){
                          case Constants.STICKY_TYPE:
                              ((NoteViewHolder) holder).titleOrContent.setPaintFlags(((NoteViewHolder) holder).titleOrContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                              break;
                          case Constants.NOTE_TYPE:
                              ((NoteViewHolder) holder).titleNoteCard1.setPaintFlags(((NoteViewHolder) holder).titleNoteCard1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                              break;
                      }

                  }
                  else{
                      switch(stylePosition){
                          case Constants.STICKY_TYPE:
                              ((NoteViewHolder) holder).titleOrContent.setPaintFlags(0);
                              break;
                          case Constants.NOTE_TYPE:
                              ((NoteViewHolder) holder).titleNoteCard1.setPaintFlags(0);
                              break;
                      }

                  }
        }
              else{
                  switch (stylePosition){
                      case Constants.STICKY_TYPE:
                          ((NoteViewHolder) holder).titleOrContent.setPaintFlags(0);
                          break;
                      case Constants.NOTE_TYPE:
                          ((NoteViewHolder) holder).titleNoteCard1.setPaintFlags(0);
                          break;
                  }

              }


              //****************************************************************************************************************
              if (notes.getIdTypeDocument() == TASK) {
                  switch(notes.getTypeReminder()){
                      case 1:
                      case 3:
                          ((NoteViewHolder)holder).indicator5.setVisibility(View.VISIBLE);
                          break;
                      case 2:
                          ((NoteViewHolder) holder).indicator3.setVisibility(View.VISIBLE);
                          break;
                      case 4:
                          ((NoteViewHolder) holder).indicator4.setVisibility(View.VISIBLE);
                          break;
                      default:
                          ((NoteViewHolder) holder).indicator4.setVisibility(View.GONE);
                          ((NoteViewHolder)holder).indicator5.setVisibility(View.GONE);
                          ((NoteViewHolder) holder).indicator3.setVisibility(View.GONE);
                          break;
                  }

                  ((NoteViewHolder) holder).taskIndicator.setVisibility(View.VISIBLE);
                  ((NoteViewHolder) holder).tsIndicator2.setVisibility(View.GONE);
                  if (notes.getStateLock() == 1) {
                      ((NoteViewHolder) holder).tsIndicator2.setVisibility(View.VISIBLE);
                      if(stylePosition == Constants.NOTE_TYPE) {
                          ((NoteViewHolder) holder).ivSecurity.setVisibility(View.VISIBLE);
                      }
                      ((NoteViewHolder)holder).ivSecurity.setVisibility(View.VISIBLE);
                  } else {
                      ((NoteViewHolder) holder).tsIndicator2.setVisibility(View.GONE);
                      if(stylePosition == Constants.NOTE_TYPE) {
                         ((NoteViewHolder) holder).ivSecurity.setVisibility(View.GONE);
                      }
                  }

              } else if(notes.getIdTypeDocument()==NOTE){
                  switch(notes.getTypeReminder()){
                      case 1:
                      case 3:
                          ((NoteViewHolder)holder).indicator5.setVisibility(View.VISIBLE);
                          break;
                      case 2:
                          ((NoteViewHolder) holder).indicator3.setVisibility(View.VISIBLE);
                          break;
                      case 4:
                          ((NoteViewHolder) holder).indicator4.setVisibility(View.VISIBLE);
                          break;
                      default:
                          ((NoteViewHolder) holder).indicator4.setVisibility(View.GONE);
                          ((NoteViewHolder)holder).indicator5.setVisibility(View.GONE);
                          ((NoteViewHolder) holder).indicator3.setVisibility(View.GONE);
                          break;
                  }

                  ((NoteViewHolder) holder).taskIndicator.setVisibility(View.GONE);
                  ((NoteViewHolder) holder).tsIndicator2.setVisibility(View.GONE);
                  if (notes.getStateLock() == 1) {
                      ((NoteViewHolder) holder).tsIndicator2.setVisibility(View.VISIBLE);
                      if(stylePosition == Constants.NOTE_TYPE) {
                          ((NoteViewHolder) holder).ivSecurity.setVisibility(View.VISIBLE);
                      }
                  } else {
                      ((NoteViewHolder) holder).tsIndicator2.setVisibility(View.GONE);
                      if(stylePosition == Constants.NOTE_TYPE) {
                         ((NoteViewHolder) holder).ivSecurity.setVisibility(View.GONE);
                      }

                  }
              }

              ActionsInControls.showColorAndFontInCardView(holder.itemView, notes, stylePosition);
              ((NoteViewHolder) holder).noteCardView.setOnClickListener(view -> {
                 if(actionMode==null) {
                     event.onClick(notes);
                 }
                else{
                    //listNotesForActionMode.add(notes);

                     toggleSelectionAndSetNum(position,notes);
                 }

              });
              ((NoteViewHolder) holder).noteCardView.setOnLongClickListener(v -> {
                    if(actionMode==null) {
                        actionMode = ((AppCompatActivity) activity).startSupportActionMode(new ContextualCallBack(NoteAdapterRecyclerView.this, notes, (NoteViewHolder) holder));
                        //listNotesForActionMode.add(notes);
                        //implementando seleccion item
                    }
                  ((NoteViewHolder) holder).itemView.setActivated(selectedItems.get(position, true));
                  toggleSelectionAndSetNum(position,notes);

                  vibrationLongPress();

                  return true;
              });
          }

    }
    private void vibrationLongPress(){
        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);
    }
    /*
    * prueba de implementación resaltodo de item seleccionado
    *
    * */
    public void toggleSelection(int pos,Note note) {
        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            if(listNotesForActionMode.contains(note)){
                listNotesForActionMode.remove(note);
            }
        } else {
            selectedItems.put(pos, true);
            if(!listNotesForActionMode.contains(note)){
                listNotesForActionMode.add(note);
            }
        }
        notifyItemChanged(pos);
    }
    private void toggleSelectionAndSetNum(int pos, Note note){


        toggleSelection(pos, note);

        int count = getSelectedItemsCount();

        if (count == 0) {
            actionMode.finish();
            actionMode = null;
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }

    }

    public void clearSelection(){
        selectedItems.clear();
        notifyDataSetChanged();
    }
    public int getSelectedItemsCount(){
        return selectedItems.size();
    }
    public List getSelectedItems() {
        List items =
                new ArrayList(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }
    /**
     *
     * fin de la prueba
     * **/
    public void addNote(Note note){
        if(!listNotes.contains(note)){
            listNotes.add(note);
           listNotesFiltered.add(0,note);
            int indexFilter=listNotesFiltered.indexOf(note);
            int index=listNotes.indexOf(note);
            notifyItemInserted(0);
            rvListNotes.smoothScrollToPosition(0);

        }

        else {
            updateNote(note);
        }

    }
    public void updateNote(Note note){
        if(listNotes.contains(note)){
            int index=listNotes.indexOf(note);
            int indexFilter=listNotesFiltered.indexOf(note);
            listNotes.set(index, note);
            listNotesFiltered.set(indexFilter, note);
            notifyItemChanged(indexFilter);
        }
    }
    public void removeNote(Note note){
        if(note!=null) {
            if (listNotes.contains(note)) {
                int indexFilter=listNotesFiltered.indexOf(note);
                int index = listNotes.indexOf(note);
                listNotesFiltered.remove(note);
                listNotes.remove(note);
                listNotesForActionMode.remove(note);
                notifyItemRemoved(indexFilter);
                if(note.getIdTypeDocument()==NOTE) {
                    Snackbar.make(activity.findViewById(android.R.id.content), R.string.noteDeleteMsg, Snackbar.LENGTH_SHORT).show();
                }
                else if(note.getIdTypeDocument()==TASK){
                    Snackbar.make(activity.findViewById(android.R.id.content), R.string.taskListDeletedMsg, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }
    public void addTask(Task task){
        if(!taskList.contains(task)){
            taskList.add(task);
        }

        else{
            updateTask(task);
        }
    }
    public void updateTask(Task task){
        if(taskList.contains(task)){
            int index=taskList.indexOf(task);
            taskList.set(index, task);

        }
    }
    public void removeTask(ArrayList<Task> tasks){
        if(tasks!=null){
            for(Task t:tasks){
                if(t.getStateDelete()==1){
                    Toast.makeText(activity, t.getContentTaskList(), Toast.LENGTH_SHORT).show();
                    if(taskList.contains(t)){
                        taskList.remove(t);
                }
            }
        }

        }
    }

    public void addAllTask(ArrayList<Task> tasks){

        for(Task task:tasks){
            addTask(task);

        }

        //no quites esto si quieres que se mantenga el cambio de las tareas eliminadas en la home
        //taskList=tasks;

    }


    //***Métodos para agregar, actualizar , eliminar desde el adapter con los datos recibidos del livedata

    public void addAllNotes(ArrayList<Note> notes){

        for (Note note:notes
        ) {
            addNote(note);

        }
        listNotesFiltered=notes;
    }

    private void shareItemContent(Note note, int typeDocument)
    {

        Intent shareIntent= new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if(typeDocument==1) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, note.getContenidoNota());

        }
        else if (typeDocument==2)
        {
             ArrayList<String> listShare= new ArrayList<>();

             for(Task ts: taskList)
             {
                 if(ts.getIdNoteReference()==note.getIdNota())
                 {
                     listShare.add(ts.getContentTaskList());
                 }
             }
            shareIntent.putExtra(Intent.EXTRA_TEXT, listShare.toString());
        }
        shareIntent.setType("text/plain");
        Intent sendIntent=Intent.createChooser(shareIntent,activity.getString(R.string.titleShareContent));
        activity.startActivity(sendIntent);
    }
    private int deleteItem( ArrayList<Note> listNotes)
    {
        ArrayList<Note> listNewNotes=new ArrayList<>();
        listNewNotes.addAll(listNotes);
        final int[] result = {0};
        Note notes = new Note();
        AlertDialog.Builder dialog=new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.warning);
        if(listNotes.size() == 1) {
            notes=listNotes.get(0);
            if (notes.getIdTypeDocument() == 1) {
                dialog.setMessage(R.string.dialogMessage);
            }
            else {
                dialog.setMessage(R.string.taskMessage);
            }
        }

        else{
            dialog.setMessage(R.string.deleteGroupItems);
        }
        dialog.setIcon(R.drawable.ic_action_warning);
        Note finalNotes = notes;
        dialog.setPositiveButton(R.string.positiveDialog, (dialog1, which) -> {

            Note nt= new Note();

            //***Eliminamos también los recordatorios y notificaciones
            if(listNotes.size()==1) {

                CloseNotificationHelper.closeNotificationHandler(activity, nt.getIdNota(), nt.getTypeReminder());
            }
            else{
                for(Note n:listNewNotes){
                    CloseNotificationHelper.closeNotificationHandler(activity, n.getIdNota(), n.getTypeReminder());
                }
            }
            //****************************************
            for(Note n:listNewNotes) {
                //deleteItem(n);
                Note obj= new Note();
                obj.setIdNota(n.getIdNota());
                dbInstance.deleteNote(obj);
                removeNote(n);

            }

            //removeNote(notes);


            if(listNotes.size()==1) {
                if (finalNotes.getIdTypeDocument() == 1) {
                    Snackbar.make(activity.findViewById(android.R.id.content), activity.getResources().getString(R.string.noteDeleteMsg), Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(activity.findViewById(android.R.id.content), activity.getResources().getString(R.string.taskListDeletedMsg), Snackbar.LENGTH_SHORT).show();

                }
            }
            else{
                Snackbar.make(activity.findViewById(android.R.id.content), activity.getResources().getString(R.string.notesDeleted), Snackbar.LENGTH_SHORT).show();
            }

            result[0] =1;
            if(actionMode!=null){
                actionMode.finish();
            }
            callbackDismiss.dismissDialog();
        });
        dialog.setNegativeButton(R.string.negativeDialog, (dialog12, which) ->{
        if(actionMode!=null){
            actionMode.finish();
        }
                Snackbar.make(activity.findViewById(android.R.id.content),R.string.msgSnackCancel,Snackbar.LENGTH_SHORT).show();
        });
        dialog.show();

        return result[0];
    }


    private void showColorOption(Note note)
    {

        bindDialogColor= OptionsColorsPopupBinding.inflate(activity.getLayoutInflater());
        Dialog dialog= new Dialog(activity);
        dialog.setContentView(bindDialogColor.getRoot());
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();

        bindDialogColor.ivOptionYellow.setOnClickListener(v -> {
            changeColorNote(note, YELLOW, dialog);
            if(actionMode!=null){
                actionMode.finish();
            }

        });
        bindDialogColor.ivOptionOrange.setOnClickListener(v -> {
           changeColorNote(note, ORANGE, dialog);
            if(actionMode!=null){
                actionMode.finish();
            }
        });
        bindDialogColor.ivOptionGreen.setOnClickListener(v -> {
           changeColorNote(note, GREEN, dialog);
            if(actionMode!=null){
                actionMode.finish();
            }
        });
        bindDialogColor.ivOptionLightBlue.setOnClickListener(v -> {
         changeColorNote(note, BLUE, dialog);
            if(actionMode!=null){
                actionMode.finish();
            }
        });
        bindDialogColor.ivOptionViolet.setOnClickListener(v -> {
            changeColorNote(note,VIOLET, dialog);
            if(actionMode!=null){
                actionMode.finish();
            }
        });
        bindDialogColor.ivOptionPink.setOnClickListener(v -> {
            changeColorNote(note,PINK, dialog);
            if(actionMode!=null){
                actionMode.finish();
            }
         });
    }
    private void changeColorNote(Note note, int color, Dialog dialog){
        note.setIdColor(color);
        dbInstance.updateNote(note);
        updateNote(note);
        dialog.dismiss();

    }

    @Override
    public int getItemCount() {
       if(listNotesFiltered!=null && listNotesFiltered.size()>0) {
            return listNotesFiltered.size();
       }
       return 0;
    }

    @Override
    public Filter getFilter() {

       return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence valueFilter) {


                FilterResults results = new FilterResults();
                listNotesFiltered.clear();


                ArrayList<Note> filterList = new ArrayList();
                if (valueFilter.length() == 0) {
                    listNotesFiltered.clear();
                    results.values = listNotes;
                    results.count = listNotes.size();
                    listNotesFiltered.addAll(listNotes);

                } else {


                    String filterNoteObject = valueFilter.toString().toLowerCase();

                    for (Note notes : listNotes) {

                        for (Task task : taskList) {
                            if (task.getContentTaskList().toLowerCase().contains(filterNoteObject)) {

                                if (notes.getIdNota() == task.getIdNoteReference()) {

                                    filterList.add(notes);
                                    listNotesFiltered.add(notes);
                                }
                            }


                        }
                        if (notes.getContenidoNota().toLowerCase().contains(filterNoteObject) || notes.getTituloNota().toLowerCase().contains(filterNoteObject)) {
                            listNotesFiltered.add(notes);
                            filterList.add(notes);
                        }


                    }
                    int count = 0;
                    for (Note n : filterList) {

                        int noteFound = Collections.frequency(filterList, n);
                        if (noteFound > 1) {
                            Note nt = filterList.get(count);
                            int index = listNotesFiltered.indexOf(nt);

                            int repeat = Collections.frequency(listNotesFiltered, n);
                            if (repeat > 1) {
                                listNotesFiltered.remove(index);

                            }
                        } else {
                            if (!listNotesFiltered.contains(n)) {
                                listNotesFiltered.add(n);
                            }
                        }
                        count++;
                    }


                }

                results.values = listNotesFiltered;
                results.count = listNotesFiltered.size();

                return results;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                listNotesFiltered=(ArrayList<Note>)results.values;
                notifyDataSetChanged();
            }
        };

    }




    public class NoteViewHolder extends RecyclerView.ViewHolder{
        //elementos del primer card view
        private CardView noteCardView;
         View noteView;
        private TextView titleOrContent, dateNote, titleNoteCard1;
        private ImageView taskIndicator, tsIndicator2, indicator3, indicator4, indicator5, ivSecurity;

        //***********************************

        public NoteViewHolder(@NonNull View item) {
            super(item);
            noteCardView=item.findViewById(R.id.noteCardView);
            noteView=item.findViewById(R.id.notesSeparatorView);
            titleOrContent=item.findViewById(R.id.contentNoteTextView);
            dateNote=item.findViewById(R.id.fechaNoteTextView);
            taskIndicator=item.findViewById(R.id.taskListIndicator1);
            tsIndicator2=item.findViewById(R.id.taskListIndicator2);
            titleNoteCard1=item.findViewById(R.id.titleNoteCard1);
            indicator3=item.findViewById(R.id.taskReminderIndicator3);
            indicator4=item.findViewById(R.id.taskReminderEventIndicator4);
            indicator5=item.findViewById(R.id.taskReminderEventIndicator5);
            ivSecurity=item.findViewById(R.id.ivSecurity);

        }

    }
    class ContextualCallBack implements ActionMode.Callback{
        NoteAdapterRecyclerView holder;
        Note note;
        NoteViewHolder h;

        Context context;
        public ContextualCallBack(NoteAdapterRecyclerView holder, Note notes, NoteViewHolder h) {
            this.holder=holder;
            this.note=notes;
            this.h=h;
            context = MyApp.getContext();

        }


        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.action_mode_menu,menu);
            holder.action_mode=true;

            //holder.notifyDataSetChanged();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
           //actionMode.setTitle(R.string.app_name);
            MenuItem  itemShare, itemEdit, itemColor;
            itemShare= menu.findItem(R.id.itemShareMain);
            itemEdit=menu.findItem(R.id.itemEditMain);
            itemColor=menu.findItem(R.id.itemColorMain);

            Drawable iconShare, iconColor, iconEdit;
            iconShare=context.getResources().getDrawable(R.drawable.ic_action_share_white);
            iconColor=context.getResources().getDrawable(R.drawable.ic_action_color_palette);
            iconEdit=context.getResources().getDrawable(R.drawable.ic_action_edit);
            // any text will be automatically disabled
            if(getSelectedItemsCount()>1){
                iconShare.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                iconColor.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                iconEdit.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                itemShare.setEnabled(false); // any text will be automatically disabled
                itemEdit.setEnabled(false);
                itemColor.setEnabled(false);
            }
            else {
                iconShare.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                iconColor.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                iconEdit.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

                itemShare.setEnabled(true); // any text will be automatically disabled
                itemEdit.setEnabled(true);
                itemColor.setEnabled(true);
            }
            itemShare.setIcon(iconShare);
            itemEdit.setIcon(iconEdit);
            itemColor.setIcon(iconColor);


            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.itemShareMain:
                    holder.shareItemContent(note,note.getIdTypeDocument());
                    actionMode.finish();
                    break;
                case R.id.itemEditMain:
                    event.onClick(note);
                    actionMode.finish();
                    break;
                case R.id.itemColorMain:
                    holder.showColorOption(note);

                    break;
                case R.id.itemDeleteMain:
                    holder.deleteItem(listNotesForActionMode);

                    break;

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            clearSelection();
            stateMenu=true;
            holder.action_mode=false;
            holder.actionMode = null;

        }
    }

}
