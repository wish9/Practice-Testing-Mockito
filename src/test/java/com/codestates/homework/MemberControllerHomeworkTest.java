package com.codestates.homework;

import com.codestates.member.dto.MemberDto;
import com.codestates.member.entity.Member;
import com.codestates.member.mapper.MemberMapper;
import com.codestates.member.service.MemberService;
import com.codestates.stamp.Stamp;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerHomeworkTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Gson gson;
    @MockBean // Mock 객체만들어서 주입해 줌 (Mocking할 수 있게 해줌)
    private MemberService memberService;
    @Autowired
    private MemberMapper mapper;

    @Test
    void postMemberTest() throws Exception {
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com",
                "홍길동",
                "010-1234-5678");

        Member member = mapper.memberPostToMember(post);
        member.setMemberId(1L);

        given(memberService.createMember(Mockito.any(Member.class))) // Mock객체의 createMember()메서드에 매개변수로 any Member객체가 들어가면
                .willReturn(member); // member 객체를 리턴하게 만드는 것

        String content = gson.toJson(post);

        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        actions
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))));
    }

    @Test
    void patchMemberTest() throws Exception {
        MemberDto.Patch patch = new MemberDto.Patch(1L, "홍길동", "010-1234-5678", Member.MemberStatus.MEMBER_ACTIVE);

        Member member = mapper.memberPatchToMember(patch);
        member.setEmail("hgd@gmail.com");
        member.setStamp(new Stamp());
        System.out.println("member = " + member);

        given(memberService.updateMember(Mockito.any(Member.class)))
                .willReturn(member);

        String patchContent = gson.toJson(patch);

        mockMvc.perform(
                patch("/v11/members/"+member.getMemberId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchContent)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.data.name").value(patch.getName()),
                jsonPath("$.data.phone").value(patch.getPhone())
                );


    }

    @Test
    void getMemberTest() throws Exception {
        Member member = makeMember();

        given(memberService.findMember(Mockito.anyLong()))
                .willReturn(member);

        mockMvc.perform(
                get("/v11/members/"+member.getMemberId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.data.name").value(member.getName()),
                jsonPath("$.data.phone").value(member.getPhone())
        );

    }

    @Test
    void getMembersTest() throws Exception {
        Member member1 = makeMember();
        Member member2 = makeMember2();

        int page = 1;
        int size = 10;

        Page<Member> list = new PageImpl<>(List.of(member1,member2), PageRequest.of(page,size, Sort.by("memberId").descending()),2);

        given(memberService.findMembers(Mockito.anyInt(),Mockito.anyInt()))
                .willReturn(list);

        mockMvc.perform(
                get("/v11/members?page="+page+"&size="+size)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(
                        status().isOk(),
                        jsonPath("$.data[0].email").value(member1.getEmail()), // 페이지네이션 정렬에 따른 순서 주의
                        jsonPath("$.data[1].email").value(member2.getEmail())
                );
    }

    @Test
    void deleteMemberTest() throws Exception { // 결론적으로 spring연결 확인하는 정도 밖에 의미 없음

        doNothing().when(memberService).deleteMember(Mockito.anyLong()); // 그나마 하나 있는 기능 아무것도 안하게 만들기

        mockMvc.perform(
                delete("/v11/members/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }

    public Member makeMember(){
        Member member = new Member("hgd@gmail.com", "홍길동", "010-1234-5678");
        member.setStamp(new Stamp());
        member.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);
        member.setMemberId(1L);

        return member;
    }

    public Member makeMember2(){
        Member member = new Member("hgd2@gmail.com", "둘길동", "010-2222-2222");
        member.setStamp(new Stamp());
        member.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);
        member.setMemberId(2L);

        return member;
    }
}
