package com.library.service;

import com.library.dto.board.BoardCreateDto;
import com.library.dto.board.BoardDetailDto;
import com.library.dto.board.BoardListDto;
import com.library.dto.board.BoardUpdateDto;
import com.library.entity.board.Board;
import com.library.entity.board.BoardFile;
import com.library.entity.board.BoardStatus;
import com.library.entity.member.Member;
import com.library.repository.BoardRepository;
import com.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/*
    게시글 Service
        - 게시글 관련 비지니스 로직을 처리함
        - 트랜잭션 관리 및 Entity와 DTO 간 변환을 담당함
        - N+1 문제 해결
            - 게시글 목록 조회 시 작성자 정보(author)도 함께 조회
            - Fetch Join을 사용하는 repository메소드 활용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;
    /*
        게시글 목록 조회(페이징)
            - ACTIVE 상태의 게시글만 조회하며 최신순으로 정렬함
            - Entity를 DTO로 변환하여 반환
            - N+1 문제 해결
                - findByStatusWithAuthor() 메소드 사용
                - Board와 Member를 Join으로 한 번에 조회
                - BoardListDto 변환시 author.getName() 호출해도 추가 쿼리 없음
        @param page 조회할 페이지 변호(0부터 시작)
        @param size 페이지당 게시글 수
        @return 페이징 된 게시글 목록(BoardListDto)
     */
    public Page<BoardListDto> getBoardList(int page, int size) {
        /*
            ACTIVE상태의 게시글 조회
            Entity -> DTO
            page.map() : page 내부의 각 board entity를 BoardListDto로 변환
            BoardListDto::from - 메소드 레퍼런스 (Board -> BoardListDto.from(board))
         */
    	return boardRepository.findByStatusWithAuthor(BoardStatus.ACTIVE,PageRequest.of(page, size, Sort.by("createdAt").descending())).map(BoardListDto::from);
    }
    /*
        게시글 상세 조회
            - ACTIVE 상태의 게시글만 조회
            - 조회수를 1 증가시킴 (더티 체킹으로 자동 반영)
            - 존재하지 않거나 삭제된 게시글은 예외 발생
        @Transactional (더티 체킹의 핵심 키워드)
            - readOnly = false (기본값)
                - 조회수 증가를 위한 쓰기 작업이 필요하므로 readOnly를 사용하지 않음
                - 트랜젝션 내에서 엔티티 변경 => 더티 체킹으로 자동 UPDATE
     */
    @Transactional  // 더티 체킹을 위해 필요함
    public BoardDetailDto getBoard(Long id) {
        // 1. DB 게시글 조회
        Board board = boardRepository.findByIdAndStatusWithAuthor(id, BoardStatus.ACTIVE).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        // 2. 조회수 증가(메모리 상에서만)
        board.increaseViewCount();
        // 3. 변환
        return BoardDetailDto.from(board);
        // 4. 메소드 종료 - 트랜잭션 커밋 직전 더티 체킹 실행
        /*
            JPA가 스냅샷과 현재 엔티티를 비교하여 viewCount 변경 감지
            UPDATE board Set view_count=?,...
         */
    }
    /*
        게시글 작성
            - 새로운 게시글을 생성하여 DB에 저장함
            - 현재 로그인 한 사용자를 작성자로 설정
                - spring security에서 현재 로그인한 사용자의 이메일 가져옮
            - 첨부파일이 있으면 같이 저장
            - 초기 상태는 ACTIVE, 조회수, 좋아요는 0으로 설정
            - @Transactional
                - readOnly = false 기본값
                - 쓰기 작업이므로 트랜잭션 필요
                - save() 호출 후 자동으로 커밋
            - 동작 과정
                1) spring security(securityContext)에서 현재 사용자 이메일 추출
                2) 이메일로 Member 조회
                3) dto 데이터 + member로 board 엔티티 생성
                4) 첨부파일이 있으면
                    - 각 파일을 서버에 저장
                    - boardFile 엔티티 생성
                    - board에 파일 추가(양방향 관계)
                5) boardRepository.save로 db 저장
                6) 쿼리 실행
                7) 생성된 게시글 id 반환
     */
    @Transactional
    public Long createBoard(BoardCreateDto boardCreateDto, String userEmail) {
        // 1) 현재 로그인한 사용자 정보 조회
        Member author = memberRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 2) Board 엔티티 생성
        Board board = Board.builder()
                .title(boardCreateDto.getTitle())
                .content(boardCreateDto.getContent())
                .category(boardCreateDto.getCategory())
                .author(author)
                .build();
        // 3) 첨부파일 처리
        if (boardCreateDto.getFiles() != null && !boardCreateDto.getFiles().isEmpty()) {
            for (MultipartFile file : boardCreateDto.getFiles()) {
                if (file.isEmpty()) {
                    continue;
                }
                String[] fileInfo = fileStorageService.storeFile(file, "boards");
                String storedFileName = fileInfo[0];    // UUID.extension
                String filePath = fileInfo[1];          // 전체 경로
                BoardFile boardFile = BoardFile.builder()
                        .originalFileName(file.getOriginalFilename())
                        .storedFileName(storedFileName)
                        .filePath(filePath)
                        .fileSize(file.getSize())
                        .fileExtension(fileStorageService.getFileExtension(file.getOriginalFilename()))
                        .mimeType(file.getContentType())    // 파일 mime 타입
                        .build();
                board.addFile(boardFile);   // 양방향 관계 설정
            }
        }
        // db 저장
        Board saved = boardRepository.save(board);
        return saved.getId();
    }
    /*
        게시글 삭제 soft delete
            - 실제 데이터를 삭제하지 않고 상태만 DELETED로 변경
            - 작성자 본인만 삭제 가능
            - 더티 체킹으로 상태 변경이 DB에 자동 반영
            - 장점 : 데이터 복구 가능, 감사 추적 유지, 통계 데이터 보존, 외래키 제약 조건 유지
     */
    @Transactional
    public void deleteBoard(Long id, String userEmail) {
        // 1. 게시글 조회 (작상자 정보 포함)
        Board board = boardRepository.findByIdAndStatusWithAuthor(id, BoardStatus.ACTIVE).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        // 2. 권한 검증 - 작성자 본인만 삭제 가능
        if (!board.getAuthor().getEmail().equals(userEmail)) {
            throw new RuntimeException("게시글을 삭제할 권한이 없습니다.");
        }
        // 3) soft delete 상태만 변경
        board.delete();
        // 4) 메소드 종료 - 트랜잭션 커밋 직전 더티체킹 실행
        /*
            JPA가 스냅샷과 현재 엔티티를 비교하여 status 변경 감지
            UPDATE SQL 쿼리 실행
         */
    }
    /*
        게시글 수정용 조회
            - 수정 폼에 표시할 게시글 정보 조회
            - 작성자 본인만 조회 가능
            - Active 상태만
     */
    @Transactional(readOnly = true)
    public BoardDetailDto getBoardForEdit(Long id, String userEmail) {
        // 1) 게시글 조회 (작성자 정보 포함)
        Board board = boardRepository.findByIdAndStatusWithAuthor(id, BoardStatus.ACTIVE).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        // 2) 권한 검증 - 작성자 본인만 수정 가능
        if (!board.getAuthor().getEmail().equals(userEmail)) {
            throw new RuntimeException("게시글을 수정 할 권한이 없습니다.");
        }
        // 3) DTO로 변환하여 반환
        return BoardDetailDto.from(board);
    }
    /*
        게시글 수정
            - 제목, 내용, 카테고리 수정
            - 기존 파일 삭제 및 새 파일 추가 처리
            - 작성자 본인만 수정 가능
            - Transactional 처리
     */
    @Transactional
    public void updateBoard(Long id, String userEmail, BoardUpdateDto boardUpdateDto) {
        // 1) 게시글 조회
        Board board = boardRepository.findByIdAndStatusWithAuthor(id, BoardStatus.ACTIVE).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        // 2) 권한 검증
        if (!board.getAuthor().getEmail().equals(userEmail)) {
            throw new RuntimeException("게시글을 수정 할 권한이 없습니다.");
        }
        // 3) 게시글 기본 정보 수정 (더티 체킹으로 자동 update)
        board.update(boardUpdateDto);
        // 4) 기존 파일 삭제 처리
        if (boardUpdateDto.getFiles() != null && !boardUpdateDto.getFiles().isEmpty()) {
            // 삭제 할 파일 ID 목록을 순회
            for (Long fileId : boardUpdateDto.getDeleteFileIds()) {
                board.getFiles().stream().filter(file -> file.getId().equals(fileId)).findFirst().ifPresent(file -> {
                    // 물리적 파일 삭제
                    fileStorageService.deleteFile(file.getFilePath(), file.getStoredFileName());
                    // 컬렉션에서 제거 -- DB에서도 삭제
                    board.getFiles().remove(file);
                });
            }
        }
        // 5) 새파일 추가 처리
        if (boardUpdateDto.getFiles() != null && !boardUpdateDto.getFiles().isEmpty()) {
            for (MultipartFile file : boardUpdateDto.getFiles()) {
                // 빈 파일 건너 뛰기
                if (file.isEmpty()) {
                    continue;
                }
                String[] fileInfo = fileStorageService.storeFile(file, "boards");
                String storedFileName = fileInfo[0];
                String filePath = fileInfo[1];
                BoardFile boardFile = BoardFile.builder()
                        .originalFileName(file.getOriginalFilename())
                        .storedFileName(storedFileName)
                        .filePath(filePath)
                        .fileSize(file.getSize())
                        .fileExtension(fileStorageService.getFileExtension(file.getOriginalFilename()))
                        .mimeType(file.getContentType())
                        .downloadCount(0L)
                        .build();
                board.addFile(boardFile);
            }
        }
        // 메소드 종료
        // JPA가 변경사항을 감지하여 자동으로 update 실행
    }
}
