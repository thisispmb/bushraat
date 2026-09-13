document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".password-toggle").forEach((toggle) => {
        const targetId = toggle.dataset.target || "password";
        const password = document.getElementById(targetId);

        if (!password) {
            return;
        }

        toggle.addEventListener("click", () => {
            const visible = password.type === "text";

            password.type = visible ? "password" : "text";

            toggle.setAttribute(
                "aria-label",
                visible ? "Show password" : "Hide password"
            );

            toggle.innerHTML = visible
                ? `<svg viewBox="0 0 24 24" aria-hidden="true">
                       <path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z"/>
                       <circle cx="12" cy="12" r="2.5"/>
                   </svg>`
                : `<svg viewBox="0 0 24 24" aria-hidden="true">
                       <path d="m3 3 18 18"/>
                       <path d="M10.6 6.2A10.8 10.8 0 0 1 12 6c6 0 9.5 6 9.5 6a17.8 17.8 0 0 1-3.1 3.6"/>
                       <path d="M6.1 6.8C3.8 8.2 2.5 12 2.5 12s3.5 6 9.5 6c1 0 1.9-.2 2.7-.5"/>
                   </svg>`;
        });
    });
});
