package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class Dashboard extends PollingBaseActivity {

    /* ---------- UI ---------- */
    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;
    private TextView helloNameTextView;          // added – was missing
    private TextView totalMenuCountText, totalReservationsText,
            pendingReservationsText, cancelledReservationsText;
    private LinearLayout pendingTableContainer;

    /* ---------- DATA ---------- */
    private MenuDatabaseHelper menuDb;
    private ReservationDatabaseHelper resDb;
    private UserService userService;

    /* ---------- LIFE-CYCLE ---------- */
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String studentId = prefs.getString("logged_in_student_id", "");
        String userId = prefs.getString("logged_in_user_id", "");
        setContentView(R.layout.activity_dashboard);

        initData();
        initViews();
        initListeners();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshAll();
    }

    /* ---------- INIT ---------- */
    private void initData() {
        menuDb = new MenuDatabaseHelper(this);
        resDb  = new ReservationDatabaseHelper(this);
        userService = new UserService(this);
    }

    private void initViews() {
        /* header */
        drawerLayout  = findViewById(R.id.main);
        menuButton    = findViewById(R.id.side_nav_button);
        closeButton   = findViewById(R.id.closeSideNav);
        profileHeader = findViewById(R.id.profile_nav);

        /* stats */
        totalMenuCountText       = findViewById(R.id.total_menu_items);
        totalReservationsText    = findViewById(R.id.total_reservations);
        pendingReservationsText  = findViewById(R.id.total_pending_reservation);
        cancelledReservationsText= findViewById(R.id.cancelled_reservations);

        /* pending table */
        pendingTableContainer = findViewById(R.id.pending_table_container);

        /* side-nav greeting */
        View sideNav = findViewById(R.id.dashboard_nav);   // your <include> tag root
        helloNameTextView = sideNav.findViewById(R.id.admin_name);
    }

    private void initListeners() {
        /* header */
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> open(Profile_Settings.class));

        /* side nav */
        bindNav(R.id.dashboardButton,      Dashboard.class);
        bindNav(R.id.menuButton,           Menu_Management.class);
        bindNav(R.id.reservationButton,    Reservation_Management.class);
        bindNav(R.id.profileButton,        Profile_Settings.class);
        bindNav(R.id.notificationSettingsButton, Notification_Settings.class);
        findViewById(R.id.logoutButton).setOnClickListener(v -> showLogoutDialog());

        /* cards */
        bindCard(R.id.pending_reservation,   Reservation_Management.class);
        bindCard(R.id.total_reservation,     Reservation_Management.class);
        bindCard(R.id.cancelled_reservation, Reservation_Management.class);
        bindCard(R.id.total_menu,            Menu_Management.class);

        /* footer */
        findViewById(R.id.footer_dashboard).setOnClickListener(v -> open(Dashboard.class));
        findViewById(R.id.footer_menu).setOnClickListener(v -> open(Menu_Management.class));
        findViewById(R.id.footer_reservation).setOnClickListener(v -> open(Reservation_Management.class));
    }

    /* ---------- HELPERS ---------- */
    private void bindNav(int viewId, Class<?> cls) {
        findViewById(viewId).setOnClickListener(v -> open(cls));
    }

    private void bindCard(int cardId, Class<?> cls) {
        findViewById(cardId).setOnClickListener(v -> open(cls));
    }

    private void open(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    private void refreshAll() {
        updateCounts();
        fillPendingTable();
        fetchName();
    }

    private void updateCounts() {
        totalMenuCountText.setText(String.valueOf(menuDb.getMenuCount()));
        totalReservationsText.setText(String.valueOf(resDb.countAll()));
        pendingReservationsText.setText(String.valueOf(resDb.countByStatus("Pending")));
        cancelledReservationsText.setText(String.valueOf(resDb.countByStatus("Declined")));
    }

    private void fillPendingTable() {
        pendingTableContainer.removeAllViews();
        List<Reservation> list = resDb.getPendingReservation(5); // newest 5
        if (list.isEmpty()) {
            TextView no = new TextView(this);
            no.setText("No pending reservations");
            no.setPadding(16, 16, 16, 16);
            pendingTableContainer.addView(no);
            return;
        }
        for (Reservation r : list) {
            View row = getLayoutInflater().inflate(R.layout.item_pending_row, pendingTableContainer, false);
            ((TextView) row.findViewById(R.id.row_name)).setText(r.getGuestName());
            ((TextView) row.findViewById(R.id.row_date)).setText(r.getDate());
            ((TextView) row.findViewById(R.id.row_time)).setText(r.getTime());
            ((TextView) row.findViewById(R.id.row_pax)).setText(String.valueOf(r.getGuests()));
            pendingTableContainer.addView(row);
        }
    }

    private void fetchName() {
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String stId = sp.getString("logged_in_student_id", "");
        String uId  = sp.getString("logged_in_user_id", "");
        if (stId.isEmpty() || uId.isEmpty()) return;

        userService.getUserProfile(stId, uId, new UserService.UserProfileListener() {
            @Override public void onSuccess(User u) {
                helloNameTextView.setText("Hello, " + u.getFirstname());
            }
            @Override public void onError(String msg) {
                Log.e("Dashboard", "Name fetch failed: " + msg);
            }
        });
    }

    private void showLogoutDialog() {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.activity_logout_confirmation_popup);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
        MaterialButton yes = d.findViewById(R.id.confirmCancel);
        MaterialButton no  = d.findViewById(R.id.noCancel);
        yes.setOnClickListener(v -> {
            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .edit()
                    .remove("is_logged_in")
                    .remove("logged_in_student_id")
                    .remove("logged_in_user_id")
                    .apply();
            open(MainActivity.class);
            finish();
        });
        no.setOnClickListener(v -> d.dismiss());
        d.show();
    }
}