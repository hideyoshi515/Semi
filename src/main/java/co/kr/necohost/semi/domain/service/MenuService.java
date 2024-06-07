package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.MenuRequest;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class MenuService {
    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> getAllMenus() {
        return menuRepository.findAllByOrderByCategory();
    }

    public void deleteMenuById(Long id) {
        menuRepository.deleteById(id);
    }

    public Menu getMenuById(Long id) {
        return menuRepository.findById(id).orElse(null);
    }

    public List<Menu> getMenuByCategory(int category) {
        return menuRepository.findByCategory(category);
    }

    public Menu saveMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    public void saveMenuWithImage(MenuRequest menuRequest, MultipartFile file) {
        // 파일이 저장될 곳. 실제 서버의 로컬 경로를 의미함
        String path = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\img\\menu\\";
        File pathCheck = new File(path);
        if(!pathCheck.exists()) {
            pathCheck.mkdirs();
        }
        // 서버에서 파일을 불러올 경로 설정. 다음과 같이 처리하면 serverPath는 img\menu\ 가 될 것
        String serverPath = path.split("static\\\\")[1];
        UUID uuid = UUID.randomUUID();
        // 파일명 재정의. split을 통해 확장자만 남기고 파일명은 uuid로 설정해주는 과정
        String fileName = null;
        if (!file.getOriginalFilename().isEmpty()) {
            fileName = uuid + "." + file.getOriginalFilename().split("\\.")[1];
            // 임시 파일 생성. 재정의한 파일명으로 임시 파일 객체 생성.
            // 실제 서버의 로컬 경로에, uuid로 처리한 파일
            File saveTo = new File(path + fileName);
            // 서버 경로 업데이트. 서버에서 불러올 경로를 파일 이름과 결합하여 완성
            serverPath = serverPath + fileName;

            Menu existingMenu = this.getMenuById(menuRequest.getId());
            if (existingMenu != null && existingMenu.getImage() != null) {
                File existingFile = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\static\\" + existingMenu.getImage());
                if (existingFile.exists()) {
                    existingFile.delete();
                }
            }

            try {
                // 업로드된 파일을 설정된 경로에 저장
                file.transferTo(saveTo);
                menuRepository.save(menuRequest.toEntity(serverPath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }else{
            menuRepository.save(menuRequest.toEntity(menuRepository.findById(menuRequest.getId()).get().getImage()));
        }
    }
}