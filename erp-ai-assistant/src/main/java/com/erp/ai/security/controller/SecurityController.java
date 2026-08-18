package com.erp.ai.security.controller;

import com.erp.ai.security.dto.SecurityProbeRequest;
import com.erp.ai.security.dto.SecurityProbeResponse;
import com.erp.ai.security.service.InjectionGuard;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 安全抽测 HTTP 入口（域内 Controller）。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>对外暴露安全探针接口，接收用户话术（utterance），调用 {@link InjectionGuard} 做规则检查</li>
 *   <li>将判定结果（是否拦截、类别、原因、命中标签）封装为 {@link SecurityProbeResponse} 返回</li>
 *   <li>附带固定的分层防线提示文案，便于学习对照「提示词拒答 + JSON 校验 + 工具白名单 + RAG 不可覆盖安全策略」</li>
 * </ul>
 *
 * <h2>在系统中的位置</h2>
 * <p>
 * 属于 {@code com.erp.ai.security} 包的表现层，与 chat / rag / draft / tool 等域并列。
 * 本接口为独立抽测入口；生产对话路径上，{@code ChatService} 等也会直接注入并调用同一套 {@link InjectionGuard}。
 * </p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day23：Prompt 注入 / 角色劫持 / 泄密 / 绕过审批 等话术侧早拦与标注。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * 客户端 POST /api/ai/security/probe
 *   → SecurityController.probe
 *   → InjectionGuard.inspect(utterance)
 *   → 组装 SecurityProbeResponse（含 traceId、defenseHint）
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>仅做学习版规则探针，不接公司防火墙或 WAF</li>
 *   <li>硬防线仍是「无写库存/过账工具」；本接口只负责话术侧可见的判定结果</li>
 *   <li>Controller 不做业务规则扩展，规则全部在 {@link InjectionGuard}</li>
 * </ul>
 *
 * <p>路径：{@code POST /api/ai/security/probe}</p>
 */
@RestController
@RequestMapping("/api/ai/security")
public class SecurityController {

    /** 注入防护规则引擎：对 utterance 做正则/关键词匹配并给出 Verdict。 */
    private final InjectionGuard injectionGuard;

    /**
     * 构造注入：由 Spring 注入单例 {@link InjectionGuard}。
     *
     * @param injectionGuard 话术侧注入/越权规则检查组件，不可为 null（由容器保证）
     */
    public SecurityController(InjectionGuard injectionGuard) {
        this.injectionGuard = injectionGuard;
    }

    /**
     * 对单条用户话术做安全抽测。
     * <p>
     * 业务含义：调用方提交一段可能含注入/越权意图的文本，接口返回是否应拦截及分类说明。
     * 边界：入参经 {@code @Valid} 校验 utterance 非空；本方法本身不抛业务异常，规则未命中时 blocked=false。
     * </p>
     *
     * @param request 含待检测话术 {@code utterance} 的请求体；{@code @Valid} 触发 Bean Validation
     * @return 抽测结果：traceId、blocked、category、reason、matched、defenseHint
     */
    @PostMapping("/probe")
    public SecurityProbeResponse probe(@Valid @RequestBody SecurityProbeRequest request) {
        // 1) 规则引擎检查：命中任一规则则 blocked=true，并带回首个类别与全部命中标签
        InjectionGuard.Verdict verdict = injectionGuard.inspect(request.getUtterance());

        // 2) 组装响应：每次请求生成无连字符 UUID 作为追踪 ID，便于日志关联
        SecurityProbeResponse response = new SecurityProbeResponse();
        response.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        response.setBlocked(verdict.blocked());
        // category 使用枚举名字符串（如 IGNORE_RULES），NONE 表示未命中
        response.setCategory(verdict.category().name());
        response.setReason(verdict.reason());
        response.setMatched(verdict.matched());
        // 固定教学提示：提醒学习者系统是分层防御，而非仅靠本探针
        response.setDefenseHint(
                "分层防线：提示词拒答 + JSON 校验 + 工具白名单(无写库存) + RAG 资料不可覆盖安全策略"
        );
        return response;
    }
}
