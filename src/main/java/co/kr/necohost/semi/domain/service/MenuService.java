package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.MenuRequest;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MenuService {
	private final MenuRepository menuRepository;

	public MenuService(MenuRepository menuRepository) {
		this.menuRepository = menuRepository;
	}

	// 在庫数を追加
	public void addStockOrder(long id, int amount) {
		var menu = menuRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid menu ID"));
		menu.setStockorder(menu.getStockorder() + amount);
		menuRepository.save(menu);
	}

	// ストックと注文数の更新をキャンセル
	public void cancelUpdateStockAndOrder(long id) {
		var menu = menuRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid menu ID"));
		menu.setStockorder(0);
		menuRepository.save(menu);
	}

	// メニューを削除
	public void deleteMenuById(Long id) {
		menuRepository.deleteById(id);
	}

	// すべてのメニューを取得
	public List<Menu> getAllMenus() {
		return menuRepository.findAllByOrderByIdDescCategoryAsc();
	}

	// カテゴリごとにメニューを分類
	public Map<Long, List<Menu>> getCategorizedMenus() {
		return menuRepository.findAll().stream().collect(Collectors.groupingBy(Menu::getCategory));
	}

	// カテゴリ別にメニューを取得
	public List<Menu> getMenuByCategory(int category) {
		return menuRepository.findByCategoryOrderByIdDesc(category);
	}

	// IDでメニューを取得
	public Menu getMenuById(Long id) {
		return menuRepository.findById(id).orElse(null);
	}

	// メニューを保存
	public Menu saveMenu(Menu menu) {
		return menuRepository.save(menu);
	}

	// メニューの画像付き保存
	public void saveMenuWithImage(MenuRequest menuRequest, MultipartFile file) {
		// ファイルが保存される場所
		var path = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\img\\menu\\";
		var pathCheck = new File(path);
		if (!pathCheck.exists()) {
			pathCheck.mkdirs();
		}
		// サーバー上のファイルパスを設定
		var serverPath = path.split("static\\\\")[1];
		var uuid = UUID.randomUUID();
		// ファイル名の再定義
		var fileName = !file.getOriginalFilename().isEmpty() ? uuid + "." + file.getOriginalFilename().split("\\.")[1] : null;
		if (fileName != null) {
			// 一時ファイルの生成
			var saveTo = new File(path + fileName);
			// サーバーパスの更新
			serverPath = serverPath + fileName;

			var existingMenu = this.getMenuById(menuRequest.getId());
			if (existingMenu != null && existingMenu.getImage() != null) {
				var existingFile = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\static\\" + existingMenu.getImage());
				if (existingFile.exists()) {
					existingFile.delete();
				}
			}

			try {
				// アップロードされたファイルを設定されたパスに保存
				file.transferTo(saveTo);
				menuRepository.save(menuRequest.toEntity(serverPath));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		} else {
			menuRepository.save(menuRequest.toEntity(menuRepository.findById(menuRequest.getId()).orElseThrow().getImage()));
		}
	}

	// ストックと注文数を更新
	public void updateStockAndOrder(long id) {
		var menu = menuRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid menu ID"));
		menu.setStock(menu.getStock() + menu.getStockorder());
		menu.setStockorder(0);
		menuRepository.save(menu);
	}




}
