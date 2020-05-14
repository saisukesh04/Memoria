package com.example.memoria.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.memoria.R;
import com.example.memoria.database.CommentsDatabase;
import com.example.memoria.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetViewsFactory(getApplicationContext(), intent);
    }

    class WidgetViewsFactory implements RemoteViewsFactory {

        private Context context;
        private int appWidgetId;
        private List<Comment> commentsList = new ArrayList<>();
        CommentsDatabase mDatabase;

        public WidgetViewsFactory(Context context, Intent intent) {
            this.context = context;
            this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            mDatabase = CommentsDatabase.getInstance(context);
            commentsList.clear();
            Log.i("Info: ", "Cleared");
            List<Comment> comments = mDatabase.CommentDao().loadAllComments();
            commentsList.addAll(comments);
            Log.i("Info: ", "Cleared: " + commentsList);
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return commentsList != null? commentsList.size(): 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_row_item);
            Comment comment = commentsList.get(position);
            remoteView.setTextViewText(R.id.widget_username, comment.getUsername());
            remoteView.setTextViewText(R.id.widget_comment, comment.getComment());
            return remoteView;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
