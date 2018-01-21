package com.bupt.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xusong on 2018/1/14.
 * About:
 */

public class ChessView extends View {


    private static class Location {
        int x;
        int y;

        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static List<Location> crosses = new ArrayList<>();

    static {
        crosses.add(new Location(1, 2));
        crosses.add(new Location(1, 7));
        crosses.add(new Location(7, 2));
        crosses.add(new Location(7, 7));
        int[] line = new int[]{0, 2, 4, 6, 8};
        int[] row = new int[]{3, 6};
        for (int i : line)
            for (int j : row)
                crosses.add(new Location(i, j));
    }

    //棋盘上的布局
    Piece[][] board = new Piece[9][10];
    //吃了的东西
    List<Piece> redDead = new ArrayList<>(16);
    List<Piece> blackDead = new ArrayList<>(16);

    private static final int[] soldiers = new int[]{0, 2, 4, 6, 8};
    private static final int[] cannon = new int[]{1, 7};
    private static final int[] vehicles = new int[]{0, 8};
    private static final int[] horses = new int[]{1, 7};
    private static final int[] elephants = new int[]{2, 6};
    private static final int[] guards = new int[]{3, 5};
    private static final int[] general = new int[]{4};

    private boolean isRed = true;
    private boolean redTurn = false;

    Paint framePaint = new Paint();
    Paint textPaint = new Paint();
    Paint tipPaint = new Paint();


    private float gap = -1;
    private float startX = -1;
    private float startY = -1;


    public interface OnStepMoveListener {
        public void onMove(int fx, int fy, int tx, int ty);
    }

    private Location selectedLocationInboard;

    //返回board数组中被选中的棋子，而非view中
    private Location computeLocation(MotionEvent event) {
        float x = event.getX() - startX;
        float y = event.getY() - startY;
        int px = (int) (x / gap);
        if (x - (px * gap) >= (gap / 2))
            px += 1;
        int py = (int) (y / gap);
        if (y - (py * gap) >= (gap / 2))
            py += 1;
        if (px >= 8)
            px = 8;
        if (py >= 9)
            py = 9;
        return getBySide(px, py);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (redTurn == isRed) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (selectedLocationInboard == null) {//第一次点击
                        selectedLocationInboard = computeLocation(event);
                        Piece p = board[selectedLocationInboard.x][selectedLocationInboard.y];
                        if (p == null) {//点击的位置没有东西
                            selectedLocationInboard = null;
                        } else {
                            if (p.side == 1 && !isRed)
                                selectedLocationInboard = null;
                            else if (p.side == 2 && isRed)
                                selectedLocationInboard = null;
                            else {//点击了自己的棋子
                                Toast.makeText(getContext(), "你选中了:" + p.getName(), Toast.LENGTH_SHORT).show();
                                postInvalidate();
                            }
                        }
                    } else {//意味着已经有了一个待选的棋子，现在要么走要么重选
                        Location loc = computeLocation(event);
                        Piece pie = board[loc.x][loc.y];
                        if (pie != null && pie.side == board[selectedLocationInboard.x][selectedLocationInboard.y].side) {
                            selectedLocationInboard = loc;//选了个自己的新棋子
                            Toast.makeText(getContext(), "你重新选中了:" + pie.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "你想要走到一个新地方" + loc.x + " " + loc.y, Toast.LENGTH_SHORT).show();
                        }
                        postInvalidate();
                    }
                    break;

            }
        } else {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                Toast.makeText(getContext(), "不是你的回合", Toast.LENGTH_SHORT).show();

        }

        return true;
    }


    public ChessView(Context context) {
        super(context);
    }

    public ChessView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChessView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textPaint.setStyle(Paint.Style.STROKE);
        tipPaint.setColor(Color.BLUE);
    }


    private Location getBySide(int x, int y) {
        if (isRed)
            return new Location(x, y);
        else return new Location(8 - x, 9 - y);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpec = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpec = MeasureSpec.getMode(heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpec == MeasureSpec.AT_MOST || heightSpec == MeasureSpec.AT_MOST) {
            int z = Math.min(w, h);
            float zh = (float) (z * 1.2);
            setMeasuredDimension(z, (int) zh);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        // TODO: 2018/1/14 中间一通计算 ,获得起点坐标，小方块边长
        if (startX == -1) {
            startX = width / 10;
            startY = startX / 2;
            gap = startX;
            textPaint.setTextSize(gap / 2);
        }
        float endY = startY + gap * 9;
        float endX = startX + gap * 8;
        drawBoard(canvas, startX, startY, endX, endY, gap);
        drawPieces(canvas, startX, startY, endX, endY, gap);
        drawSelectedAndTips(canvas, startX, startY, endX, endY, gap);
    }

    private void drawSelectedAndTips(Canvas canvas, float startX, float startY, float endX, float endY, float gap) {
        if (selectedLocationInboard != null) {//选中
            Location viewLocation = getBySide(selectedLocationInboard.x, selectedLocationInboard.y);
            float x = startX + viewLocation.x * gap;
            float y = startY + viewLocation.y * gap;
            float distance = gap / 2;
            float length = gap / 3;
            canvas.drawLine(x - distance, y - distance, x - distance + length, y - distance, tipPaint);
            canvas.drawLine(x - distance, y - distance, x - distance, y - distance + length, tipPaint);
            canvas.drawLine(x - distance, y + distance, x - distance + length, y + distance, tipPaint);
            canvas.drawLine(x - distance, y + distance, x - distance, y + distance - length, tipPaint);
            canvas.drawLine(x + distance, y - distance, x + distance - length, y - distance, tipPaint);
            canvas.drawLine(x + distance, y - distance, x + distance, y - distance + length, tipPaint);
            canvas.drawLine(x + distance, y + distance, x + distance - length, y + distance, tipPaint);
            canvas.drawLine(x + distance, y + distance, x + distance, y + distance - length, tipPaint);
            List<Location> locations = computeReachableLocations(selectedLocationInboard);
            if (locations.size() != 0) {
                for (Location a : locations) {
                    Location actual = getBySide(a.x, a.y);
                    float centerX = startX + actual.x * gap;
                    float centerY = startY + actual.y * gap;
                    float radius = gap / 4;
                    canvas.drawCircle(centerX, centerY, radius, tipPaint);
                }
            }
        }
    }

    private void drawPieces(Canvas canvas, float startX, float startY, float endX, float endY, float gap) {
        for (int i = 0; i <= 8; i++)
            for (int j = 0; j <= 9; j++) {
                Location l = getBySide(i, j);
                Piece pie = board[i][j];
                if (pie != null) {
                    float x = startX + l.x * gap;
                    float y = startY + l.y * gap;
                    drawPieceAt(pie, canvas, x, y);
                }
            }
    }


    private List<Location> computeReachableLocations(Location l) {
        Piece p = board[l.x][l.y];
        if (p == null)
            return new ArrayList<>();
        switch (p.state) {
            case 0:
                List<Location> locations = new LinkedList<>();
                if (p.side == 1) {//红色
                    int leftX = l.x - 1;
                    int rightX = l.x + 1;
                    int forward = l.y - 1;
                    if (l.y <= 4) {
                        Piece left = null;
                        if (leftX >= 0)
                            left = board[leftX][l.y];
                        if (leftX >= 0 && (left == null || left.side != p.side))
                            locations.add(new Location(leftX, l.y));
                        Piece right = null;
                        if (rightX <= 8)
                            right = board[rightX][l.y];
                        if (rightX <= 8 && (right == null || right.side != p.side))
                            locations.add(new Location(rightX, l.y));
                    }
                    Piece fward = null;
                    if (forward >= 0)
                        fward = board[l.x][forward];
                    if (forward >= 0 && (fward == null || fward.side != p.side))
                        locations.add(new Location(l.x, forward));
                }
                return locations;
            case 1:
                return new ArrayList<>();
            case 2:
                return getVehiclesTips(l);
            case 3:
                return getHorseTips(l);
        }
        return new ArrayList<>();
    }

    private List<Location> getHorseTips(Location l) {
        Piece current = board[l.x][l.y];
        List<Location> locations = new ArrayList<>();
        int movex = l.x - 2;
        if (movex >= 0 && board[l.x - 1][l.y] == null) {//向左走,无遮挡
            if (l.y + 1 <= 9)
                if (board[movex][l.y + 1] == null || board[movex][l.y + 1].side != current.side)
                    locations.add(new Location(movex, l.y + 1));
            if (l.y - 1 >= 0)
                if (board[movex][l.y - 1] == null || board[movex][l.y - 1].side != current.side)
                    locations.add(new Location(movex, l.y - 1));
        }
        movex = l.x + 2;
        if (movex <= 8 && board[l.x + 1][l.y] == null) {
            if (l.y + 1 <= 9)
                if (board[movex][l.y + 1] == null || board[movex][l.y + 1].side != current.side)
                    locations.add(new Location(movex, l.y + 1));
            if (l.y - 1 >= 0)
                if (board[movex][l.y - 1] == null || board[movex][l.y - 1].side != current.side)
                    locations.add(new Location(movex, l.y - 1));
        }
        int moveY = l.y - 2;//向上
        if (moveY >= 0 && board[l.x][l.y - 1] == null) {
            if (l.x - 1 >= 0)
                if (board[l.x - 1][moveY] == null || board[l.x - 1][moveY].side != current.side)
                    locations.add(new Location(l.x - 1, moveY));
            if (l.x + 1 <= 8) {
                if (board[l.x + 1][moveY] == null || board[l.x + 1][moveY].side != current.side)
                    locations.add(new Location(l.x + 1, moveY));
            }
        }
        moveY = l.y + 2;
        if (moveY <= 9 && board[l.x][l.y+1] == null) {
            if (l.x - 1 >= 0)
                if (board[l.x - 1][moveY] == null || board[l.x - 1][moveY].side != current.side)
                    locations.add(new Location(l.x - 1, moveY));
            if (l.x + 1 <= 8) {
                if (board[l.x + 1][moveY] == null || board[l.x + 1][moveY].side != current.side)
                    locations.add(new Location(l.x + 1, moveY));
            }
        }
        return locations;
    }


    private List<Location> getVehiclesTips(Location l) {
        Piece current = board[l.x][l.y];
        List<Location> locations = new ArrayList<>();
        for (int j = l.x - 1; j >= 0; j--) {
            Piece piece = board[j][l.y];
            if (piece == null || piece.side != current.side)
                locations.add(new Location(j, l.y));
            if (piece != null)//只要有单位，車一定不能穿越
                break;
        }
        for (int j = l.x + 1; j <= 8; j++) {
            Piece piece = board[j][l.y];
            if (piece == null || piece.side != current.side)
                locations.add(new Location(j, l.y));
            if (piece != null)//只要有单位，車一定不能穿越
                break;
        }
        for (int j = l.y + 1; j <= 9; j++) {
            Piece piece = board[l.x][j];
            if (piece == null || piece.side != current.side)
                locations.add(new Location(l.x, j));
            if (piece != null)//只要有单位，車一定不能穿越
                break;
        }
        for (int j = l.y - 1; j >= 0; j--) {
            Piece piece = board[l.x][j];
            if (piece == null || piece.side != current.side)
                locations.add(new Location(l.x, j));
            if (piece != null)//只要有单位，車一定不能穿越
                break;
        }

        return locations;
    }

    /**
     * @param p
     * @param canvas
     * @param x      具体的坐标了
     * @param y
     */
    private void drawPieceAt(Piece p, Canvas canvas, float x, float y) {
        if (p.side == 1)
            textPaint.setColor(Color.RED);
        else textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float top = textPaint.getFontMetrics().top;
        float bot = textPaint.getFontMetrics().bottom;
        float base = y - top / 2 - bot / 2;
        canvas.drawCircle(x, y, gap / 3, textPaint);
        canvas.drawText(p.getName(), x, base, textPaint);
    }


    private void drawBoard(Canvas canvas, float startX, float startY, float endX, float endY, float gap) {
        for (int i = 0; i <= 9; i++) {
            canvas.drawLine(startX, startY + i * gap, endX, startY + i * gap, framePaint);
        }
        canvas.drawLine(startX, startY, startX, endY, framePaint);
        canvas.drawLine(endX, startY, endX, endY, framePaint);
        for (int i = 0; i <= 8; i++) {
            canvas.drawLine(startX + i * gap, startY, startX + i * gap, startY + 4 * gap, framePaint);
            canvas.drawLine(startX + i * gap, startY + 5 * gap, startX + i * gap, endY, framePaint);
        }
        canvas.drawLine(startX + 3 * gap, startY, startX + 5 * gap, startY + 2 * gap, framePaint);
        canvas.drawLine(startX + 5 * gap, startY, startX + 3 * gap, startY + 2 * gap, framePaint);
        canvas.drawLine(startX + 3 * gap, startY + 7 * gap, startX + 5 * gap, startY + 9 * gap, framePaint);
        canvas.drawLine(startX + 5 * gap, startY + 7 * gap, startX + 3 * gap, startY + 9 * gap, framePaint);
        float crosslength = gap / 5;
        float distance = gap / 10;
        for (Location e : crosses) {
            float posX = startX + (e.x) * gap;
            float posY = startY + (e.y) * gap;
            if (e.x != 0) {//不是最左边
                canvas.drawLine(posX - distance, posY + distance, posX - distance - crosslength, posY + distance, framePaint);
                canvas.drawLine(posX - distance, posY + distance, posX - distance, posY + distance + crosslength, framePaint);
                canvas.drawLine(posX - distance, posY - distance, posX - distance - crosslength, posY - distance, framePaint);
                canvas.drawLine(posX - distance, posY - distance, posX - distance, posY - distance - crosslength, framePaint);
            }
            if (e.x != 8) {
                canvas.drawLine(posX + distance, posY - distance, posX + distance + crosslength, posY - distance, framePaint);
                canvas.drawLine(posX + distance, posY - distance, posX + distance, posY - distance - crosslength, framePaint);
                canvas.drawLine(posX + distance, posY + distance, posX + distance, posY + distance + crosslength, framePaint);
                canvas.drawLine(posX + distance, posY + distance, posX + distance + crosslength, posY + distance, framePaint);

            }
        }


    }


    public void init(boolean red) {
        isRed = red;
        resetPieces();
        postInvalidate();
    }

    private void resetPieces() {
        for (int i = 0; i <= 8; i++)
            for (int j = 0; j <= 9; j++)
                board[i][j] = null;
        reset(0, soldiers, 3, 6);
        reset(1, cannon, 2, 7);
        reset(2, vehicles, 0, 9);
        reset(3, horses, 0, 9);
        reset(4, elephants, 0, 9);
        reset(5, guards, 0, 9);
        reset(6, general, 0, 9);

    }

    private void reset(int state, int[] vertical, int black, int red) {
        for (int i : vertical) {
            Piece rp = new Piece(state, 1);
            Piece bp = new Piece(state, 2);
            board[i][red] = rp;
            board[i][black] = bp;
        }
    }


    //走一步引起界面刷新
    public void confirmNextStep(int fx, int fy, int tx, int ty) {
        Piece a = board[tx][ty];
        board[tx][ty] = board[fx][fy];
        board[fx][fy] = null;
        if (a != null) {
            if (a.side == 1)
                redDead.add(a);
            else blackDead.add(a);
        }
        redTurn = !redTurn;
        postInvalidate();

    }

}

class Piece {
    int state;//身份 0-6 兵 炮 车 马 象 士 将
    int side;//属于哪一边1 红色 2黑色

    public Piece(int state, int side) {
        this.state = state;
        this.side = side;
    }

    public String getName() {
        switch (state) {
            case 0:
                if (side == 1) return "兵";
                return "卒";
            case 1:
                return "炮";
            case 2:
                if (side == 1) return "车";
                return "車";
            case 3:
                if (side == 1) return "马";
                return "馬";
            case 4:
                if (side == 1) return "相";
                return "象";
            case 5:
                if (side == 1) return "仕";
                return "士";
            case 6:
                if (side == 1) return "帅";
                return "将";
        }
        return "无法识别的棋子 ";
    }


}