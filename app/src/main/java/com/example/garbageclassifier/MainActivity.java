package com.example.garbageclassifier;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // 垃圾数据库
    private static final List<GarbageItem> GARBAGE_DATABASE = Arrays.asList(
            new GarbageItem("报纸", "可回收垃圾"),
            new GarbageItem("塑料瓶", "可回收垃圾"),
            new GarbageItem("玻璃瓶", "可回收垃圾"),
            new GarbageItem("易拉罐", "可回收垃圾"),
            new GarbageItem("旧衣服", "可回收垃圾"),
            new GarbageItem("剩饭剩菜", "厨余垃圾"),
            new GarbageItem("果皮", "厨余垃圾"),
            new GarbageItem("茶叶渣", "厨余垃圾"),
            new GarbageItem("骨头", "厨余垃圾"),
            new GarbageItem("电池", "有害垃圾"),
            new GarbageItem("荧光灯管", "有害垃圾"),
            new GarbageItem("过期药品", "有害垃圾"),
            new GarbageItem("油漆桶", "有害垃圾"),
            new GarbageItem("温度计", "有害垃圾"),
            new GarbageItem("卫生纸", "其他垃圾"),
            new GarbageItem("尿不湿", "其他垃圾"),
            new GarbageItem("烟头", "其他垃圾"),
            new GarbageItem("陶瓷碎片", "其他垃圾"),
            new GarbageItem("一次性餐具", "其他垃圾"),
            // 更多垃圾类型可以继续添加...
            new GarbageItem("纸箱", "可回收垃圾"),
            new GarbageItem("旧书", "可回收垃圾"),
            new GarbageItem("金属餐具", "可回收垃圾"),
            new GarbageItem("鱼骨头", "厨余垃圾"),
            new GarbageItem("菜叶", "厨余垃圾"),
            new GarbageItem("过期化妆品", "有害垃圾"),
            new GarbageItem("杀虫剂", "有害垃圾"),
            new GarbageItem("塑料袋", "其他垃圾"),
            new GarbageItem("灰尘", "其他垃圾")
    );


    private AutoCompleteTextView searchInput;
    private ImageButton searchButton;
    private ScrollView resultContainer;
    private LinearLayout resultContent;
    private LinearLayout historyContainer;
    private ListView historyList;
    private LinearLayout categoriesContainer;

    private SharedPreferences sharedPreferences;
    private Set<String> searchHistory = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        resultContainer = findViewById(R.id.resultContainer);
        resultContent = findViewById(R.id.resultContent);
        historyContainer = findViewById(R.id.historyContainer);
        historyList = findViewById(R.id.historyList);
        categoriesContainer = findViewById(R.id.categoriesContainer);

        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences("GarbageSearchPrefs", MODE_PRIVATE);
        searchHistory = sharedPreferences.getStringSet("searchHistory", new HashSet<>());

        // 设置搜索自动补全
        setupAutoComplete();

        // 设置搜索按钮点击事件
        searchButton.setOnClickListener(v -> performSearch());

        // 设置输入法搜索键事件
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        // 设置文本变化监听器
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    showCategories();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 设置历史记录点击事件
        historyList.setOnItemClickListener((parent, view, position, id) -> {
            String historyItem = (String) parent.getItemAtPosition(position);
            searchInput.setText(historyItem);
            performSearch();
        });

        // 初始显示分类区域
        showCategories();
        loadSearchHistory();
    }

    private void setupAutoComplete() {
        // 从数据库中提取所有垃圾名称
        List<String> garbageNames = new ArrayList<>();
        for (GarbageItem item : GARBAGE_DATABASE) {
            garbageNames.add(item.getName());
        }

        // 设置自动补全适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                garbageNames
        );
        searchInput.setAdapter(adapter);
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "请输入垃圾名称", Toast.LENGTH_SHORT).show();
            return;
        }

        // 隐藏键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);

        // 搜索垃圾
        GarbageItem result = searchGarbage(query);
        showResult(result);

        // 添加到搜索历史
        addToSearchHistory(query);
    }

    private GarbageItem searchGarbage(String name) {
        for (GarbageItem item : GARBAGE_DATABASE) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    private void showResult(GarbageItem item) {
        if (item == null) {
            // 显示未找到结果
            resultContent.removeAllViews();
            TextView notFound = new TextView(this);
            notFound.setText("未找到该垃圾的分类信息");
            notFound.setTextSize(18);
            notFound.setTextColor(getResources().getColor(R.color.dark_green));
            notFound.setPadding(0, 16, 0, 16);
            notFound.setGravity(View.TEXT_ALIGNMENT_CENTER);
            resultContent.addView(notFound);
        } else {
            // 显示结果
            resultContent.removeAllViews();

            // 垃圾名称
            TextView nameView = new TextView(this);
            nameView.setText(item.getName());
            nameView.setTextSize(24);
            nameView.setTextColor(getResources().getColor(R.color.dark_green));
            nameView.setPadding(0, 16, 0, 16);
            nameView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            resultContent.addView(nameView);

            // 垃圾图标
            TextView iconView = new TextView(this);
            iconView.setText(getEmojiByUnicode(0x1F5D1)); // 垃圾桶图标
            iconView.setTextSize(48);
            iconView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            resultContent.addView(iconView);

            // 分类结果
            TextView resultView = new TextView(this);
            resultView.setText("属于: " + item.getCategory());
            resultView.setTextSize(20);

            // 根据类别设置颜色
            int colorId;
            switch (item.getCategory()) {
                case "可回收垃圾":
                    colorId = R.color.recyclable;
                    break;
                case "厨余垃圾":
                    colorId = R.color.kitchen;
                    break;
                case "有害垃圾":
                    colorId = R.color.harmful;
                    break;
                case "其他垃圾":
                default:
                    colorId = R.color.other;
                    break;
            }
            resultView.setTextColor(getResources().getColor(colorId));
            resultView.setPadding(0, 16, 0, 16);
            resultView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            resultContent.addView(resultView);

            // 分类说明
            TextView descView = new TextView(this);
            descView.setText(getCategoryDescription(item.getCategory()));
            descView.setTextSize(16);
            descView.setTextColor(getResources().getColor(R.color.dark_gray));
            descView.setPadding(0, 24, 0, 0);
            resultContent.addView(descView);
        }

        // 显示结果区域，隐藏其他区域
        resultContainer.setVisibility(View.VISIBLE);
        historyContainer.setVisibility(View.GONE);
        categoriesContainer.setVisibility(View.GONE);
    }

    private String getCategoryDescription(String category) {
        switch (category) {
            case "可回收垃圾":
                return "可回收物是指适宜回收利用和资源化利用的生活废弃物，主要包括废纸、废塑料、废玻璃、废金属、废旧纺织物等。";
            case "厨余垃圾":
                return "厨余垃圾是指居民日常生活及食品加工、饮食服务、单位供餐等活动中产生的垃圾，包括丢弃不用的菜叶、剩菜、剩饭、果皮、蛋壳、茶渣、骨头等。";
            case "有害垃圾":
                return "有害垃圾是指对人体健康或者自然环境造成直接或者潜在危害的生活废弃物，主要包括废电池、废荧光灯管、废温度计、废血压计、废药品及其包装物等。";
            case "其他垃圾":
            default:
                return "其他垃圾是指除可回收物、有害垃圾、厨余垃圾以外的其他生活废弃物，包括卫生纸、尿不湿、烟头、陶瓷碎片、一次性餐具等。";
        }
    }

    private String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    private void addToSearchHistory(String query) {
        searchHistory.add(query);
        saveSearchHistory();
        loadSearchHistory();
    }

    private void saveSearchHistory() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("searchHistory", searchHistory);
        editor.apply();
    }

    private void loadSearchHistory() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(searchHistory)
        );
        historyList.setAdapter(adapter);
    }

    private void showCategories() {
        resultContainer.setVisibility(View.GONE);
        historyContainer.setVisibility(View.VISIBLE);
        categoriesContainer.setVisibility(View.VISIBLE);
    }

    // 分类按钮点击事件
    public void showRecyclable(View view) {
        showGarbageList("可回收垃圾");
    }

    public void showKitchen(View view) {
        showGarbageList("厨余垃圾");
    }

    public void showHarmful(View view) {
        showGarbageList("有害垃圾");
    }

    public void showOther(View view) {
        showGarbageList("其他垃圾");
    }

    private void showGarbageList(String category) {
        resultContent.removeAllViews();

        // 添加标题
        TextView title = new TextView(this);
        title.setText(category + " (" + getGarbageCountByCategory(category) + "种)");
        title.setTextSize(20);
        title.setTextColor(getResources().getColor(R.color.dark_green));
        title.setPadding(0, 0, 0, 16);
        resultContent.addView(title);

        // 添加该分类下的所有垃圾
        for (GarbageItem item : GARBAGE_DATABASE) {
            if (item.getCategory().equals(category)) {
                LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.garbage_item, resultContent, false);

                TextView nameView = itemLayout.findViewById(R.id.garbageName);
                nameView.setText(item.getName());

                TextView categoryView = itemLayout.findViewById(R.id.garbageCategory);
                categoryView.setText(item.getCategory());

                // 设置类别颜色
                int colorId;
                switch (item.getCategory()) {
                    case "可回收垃圾":
                        colorId = R.color.recyclable;
                        break;
                    case "厨余垃圾":
                        colorId = R.color.kitchen;
                        break;
                    case "有害垃圾":
                        colorId = R.color.harmful;
                        break;
                    case "其他垃圾":
                    default:
                        colorId = R.color.other;
                        break;
                }
                categoryView.setTextColor(getResources().getColor(colorId));

                resultContent.addView(itemLayout);
            }
        }

        // 显示结果区域，隐藏其他区域
        resultContainer.setVisibility(View.VISIBLE);
        historyContainer.setVisibility(View.GONE);
        categoriesContainer.setVisibility(View.GONE);
    }

    private int getGarbageCountByCategory(String category) {
        int count = 0;
        for (GarbageItem item : GARBAGE_DATABASE) {
            if (item.getCategory().equals(category)) {
                count++;
            }
        }
        return count;
    }}