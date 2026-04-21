const {
	IN_PROGRESS_TASKS_LIMIT,
	COIN_LABEL,
	requireTeamsFile,
	parseTeams,
	getTeamMembersOrNull,
	extractStoryPointsOrNull,
	storyPointsLabel,
	getPointsFromLabel,
	ALL_STORY_POINTS_LABELS,
	INVALID_STORY_POINTS_LABEL,
	addComment,
	closeIssue,
	addLabels,
	removeLabelSafe,
	removeLabelFromIssue,
	hasLabel,
	fetchTeamBudgetTasks,
	getBudgetTasks,
	getSingleRepoProjectV2,
	getStatusFieldConfig,
	findSingleStatusOptionId,
	getProjectItemIdForIssue,
	addIssueToProject,
	setProjectItemSingleSelect,
	deleteProjectItem,
	listProjectItemsWithStatusAndAssignees,
} = require("./lib.cjs");

function instructorLogin() {
	return (process.env.INSTRUCTOR_GITHUB_USERNAME || "").trim();
}

function pingInstructor() {
	return `@${instructorLogin()}`;
}

function missingEstimationComment(userToPing) {
	return [
		`Hi @${userToPing}, it seems that the time estimation in your task is not valid.`,
		"",
		"When creating issues, make sure to **pick one from the dropdown selector**. When editing, keep this exact format in the issue body:",
		"",
		"Example:",
		"```",
		"Time estimation",
		"2 story points",
		"```",
		"",
		"You will need to **update your issue body to include a valid time estimation** before this issue can be approved.",
		"",
		"> [!TIP]",
		"> Remember that the only allowed values are: **0.25, 0.5, 1, 2, 3, 4 story points.**",
	].join("\n");
}

async function ensureTeamsOrClose({ github, context, core }, login) {
	const txt = requireTeamsFile(core);
	if (!txt) return null;

	const idx = parseTeams(txt);
	const team = getTeamMembersOrNull(idx, login);

	if (!team) {
		await addComment(
			github,
			context,
			[
				`Hi @${login}, I could not find your GitHub user in any team.`,
				"",
				`Please make sure you are logged in with the correct GitHub account. If you believe this is an error, please ping ${pingInstructor()}.`,
				"This issue will be closed for now.",
			].join("\n")
		);
		await closeIssue(github, context);
		return null;
	}

	return { idx, team };
}

/**
 * Issue created (opened)
 */
async function handleIssueOpened({ github, context, core }) {
	const issue = context.payload.issue;

	// 1) Validate issue creator is in a team.
	const ok = await ensureTeamsOrClose({ github, context, core }, issue.user.login);
	if (!ok) return;

	// 2) Validate story points format.
	const points = extractStoryPointsOrNull(issue.body || "");
	if (!points) {
		await addLabels(github, context, [INVALID_STORY_POINTS_LABEL]);
		await addComment(github, context, missingEstimationComment(issue.user.login));
		return;
	}

	// 3) Add story points label.
	const spLabel = storyPointsLabel(points);
	if (!spLabel) {
		await addLabels(github, context, [INVALID_STORY_POINTS_LABEL]);
		await addComment(
			github,
			context,
			[
				`Invalid time estimation "${points}". Allowed values are: 0.25, 0.5, 1, 2, 3, 4.`,
				"",
				missingEstimationComment(issue.user.login),
			].join("\n")
		);
		return;
	}

	await removeLabelSafe(github, context, INVALID_STORY_POINTS_LABEL);
	await addLabels(github, context, [spLabel]);
}

/**
 * Issue updated (edited)
 */
async function handleIssueEdited({ github, context, core }) {
	const issue = context.payload.issue;

	// 1) If not inbox, don't modify labels and comment.
	if (!hasLabel(issue, "inbox")) {
		await addComment(
			github,
			context,
			[
				"It seems that the issue was modified.",
				"The story points cannot be changed because this task has already been approved.",
				`If you believe this task is incorrect and needs to be changed, ping ${pingInstructor()}.`,
			].join("\n")
		);
		return;
	}

	// 2) Validate points again.
	const points = extractStoryPointsOrNull(issue.body || "");
	if (!points) {
		for (const l of (issue.labels || []).map((x) => x.name)) {
			if (ALL_STORY_POINTS_LABELS.includes(l)) {
				await removeLabelSafe(github, context, l);
			}
		}
		await addLabels(github, context, [INVALID_STORY_POINTS_LABEL]);
		await addComment(github, context, missingEstimationComment(issue.user.login));
		return;
	}

	const spLabel = storyPointsLabel(points);
	if (!spLabel) {
		await addLabels(github, context, [INVALID_STORY_POINTS_LABEL]);
		await addComment(
			github,
			context,
			[
				`Invalid time estimation "${points}". Allowed values are: 0.25, 0.5, 1, 2, 3, 4.`,
				"",
				missingEstimationComment(issue.user.login),
			].join("\n")
		);
		return;
	}

	// 3) Remove old story points labels + invalid label
	for (const l of (issue.labels || []).map((x) => x.name)) {
		if (ALL_STORY_POINTS_LABELS.includes(l) && l !== spLabel) {
			await removeLabelSafe(github, context, l);
		}
	}
	const removedInvalidStoryPoints = await removeLabelSafe(github, context, INVALID_STORY_POINTS_LABEL);

	// 4) Add new story points label
	await addLabels(github, context, [spLabel]);

	if (removedInvalidStoryPoints) {
		await addComment(
			github,
			context,
			[
				"Thank you for updating the story points. This task is now valid.",
			].join("\n")
		);
	}
}

/**
 * Inbox removed (issues.unlabeled with label "inbox"), instructor-only
 */
async function handleInboxUnlabeled({ github, context, core }) {
	const label = context.payload.label?.name || "";
	if (label.toLowerCase() !== "inbox") return;

	const instructor = instructorLogin();
	if (!instructor || context.actor.toLowerCase() !== instructor.toLowerCase()) {
		return; // only instructor action triggers
	}

	// Require exactly one linked project + Status field.
	const project = await getSingleRepoProjectV2(github, context);
	const status = await getStatusFieldConfig(github, project.id);
	const backlogOptionId = findSingleStatusOptionId(status.options, "backlog");

	const issueId = context.payload.issue.node_id;
	let itemId = await getProjectItemIdForIssue(github, issueId, project.id);
	if (!itemId) itemId = await addIssueToProject(github, project.id, issueId);

	await setProjectItemSingleSelect(github, project.id, itemId, status.fieldId, backlogOptionId);

	await addLabels(github, context, ["backlog", COIN_LABEL]);

	await addComment(
		github,
		context,
		[
			"This issue has been accepted and moved to the Product Backlog. Any team can pick it up.",
			"",
			"To assign your team to this issue, add a comment with exactly one word: `request`",
		].join("\n")
	);
}

/**
 * Issue comment created: handle "request"
 */
async function handleIssueCommentCreated({ github, context, core }) {
	const comment = context.payload.comment;
	const issue = context.payload.issue;

	const requester = comment.user?.login || "";
	if (requester.toLowerCase().endsWith("[bot]")) return;

	const bodyTrim = (comment.body || "").trim();
	if (bodyTrim.toLowerCase() !== "request") return;

	// 3) Check comment creator is in a team
	const txt = requireTeamsFile(core);
	if (!txt) return;
	const idx = parseTeams(txt);

	const team = getTeamMembersOrNull(idx, requester);
	if (!team) {
		await addComment(
			github,
			context,
			[
				`Hi @${requester}, I could not find your GitHub user in any team.`,
				"",
				`Please make sure you are logged in with the correct GitHub account. If you believe this is an error, please ping ${pingInstructor()}.`,
			].join("\n")
		);
		return;
	}

	// 4) Must be in backlog
	if (!hasLabel(issue, "backlog")) {
		await addComment(
			github,
			context,
			`Hi @${requester}, this issue is not in the backlog, so it cannot be requested yet.`
		);
		return;
	}

	// 5) Single linked project
	const project = await getSingleRepoProjectV2(github, context);
	const status = await getStatusFieldConfig(github, project.id);

	// Precompute option IDs before checking step 6 (race reduction).
	const inProgressOptionId = findSingleStatusOptionId(status.options, "in progress");

	// 6) Count requester's in-progress tasks (Status contains "in progress" AND requester is assignee)
	const items = await listProjectItemsWithStatusAndAssignees(github, project.id);
	const inProgressCount = items.filter((it) => {
		const st = (it.status?.name || "").toLowerCase();
		if (!st.includes("in progress")) return false;
		const assignees = (it.content.assignees?.nodes || []).map((a) => a.login.toLowerCase());
		return assignees.includes(requester.toLowerCase());
	}).length;

	if (inProgressCount >= IN_PROGRESS_TASKS_LIMIT) {
		await addComment(
			github,
			context,
			[
				`Hi @${requester}, you cannot self-assign this issue because your team already has **${inProgressCount} tasks** in progress.`,
				`Each team can have at most ${IN_PROGRESS_TASKS_LIMIT} tasks in progress at any given time.`,
			].join("\n")
		);
		return;
	}

	// 7) Move issue to "in progress" in the project (ensure item exists)
	const issueId = issue.node_id;
	let itemId = await getProjectItemIdForIssue(github, issueId, project.id);
	if (!itemId) itemId = await addIssueToProject(github, project.id, issueId);

	await setProjectItemSingleSelect(github, project.id, itemId, status.fieldId, inProgressOptionId);

	// 8) Assign all team members
	try {
		await github.rest.issues.addAssignees({
			owner: context.repo.owner,
			repo: context.repo.repo,
			issue_number: issue.number,
			assignees: team,
		});
	} catch (e) {
		await addComment(
			github,
			context,
			[
				"I moved the issue to In Progress, but failed to assign the whole team.",
				"This usually happens if one or more usernames are not assignable on this repository.",
				`Team entry: ${team.join(" ")}`,
			].join("\n")
		);
		throw e;
	}

	// 9) Remove backlog label
	await removeLabelSafe(github, context, "backlog");

	// 10) Confirm
	const teamStr = team.map((u) => `@${u}`).join(" ");
	await addComment(
		github,
		context,
		[
			`Assigned to team: ${teamStr}.`,
			"Moved to **In progress**.",
		].join("\n")
	);
}

/**
 * Issue closed: if not closed by a merged PR, remove from projects + comment
 */
async function handleIssueClosed({ github, context, core }) {
	const issue = context.payload.issue;

	// Detect closer (ClosedEvent) and whether it was a merged PR.
	const q = `
    query($issueId:ID!) {
      node(id:$issueId) {
        ... on Issue {
          timelineItems(last: 1, itemTypes: CLOSED_EVENT) {
            nodes {
              ... on ClosedEvent {
                closer {
                  __typename
                  ... on PullRequest { merged }
                }
              }
            }
          }
          projectItems(first: 50) {
            nodes { id project { id } }
          }
        }
      }
    }
  `;

	const res = await github.graphql(q, { issueId: issue.node_id });
	const closedEvent = res.node.timelineItems.nodes?.[0];
	const closer = closedEvent?.closer;

	const closedByMergedPR =
		closer &&
		closer.__typename === "PullRequest" &&
		closer.merged === true;

	if (closedByMergedPR) return;

	// Remove from any linked projects v2
	const items = res.node.projectItems.nodes || [];
	for (const it of items) {
		await deleteProjectItem(github, it.project.id, it.id);
	}

	const wasActuallyDeletedFromProject = items.length > 0;
	if (wasActuallyDeletedFromProject) {
		await addComment(
			github,
			context,
			[
				"This issue was closed without any merged PR.",
				"It has been removed from the project and it will not be counted for points.",
				`If you think this was a mistake, please ping ${pingInstructor()}.`,
			].join("\n")
		);
	}
}

/**
 * PR comment created: handle "ready"
 */
async function handlePullRequestCommentCreated({ github, context, core }) {
	const comment = context.payload.comment;
	const issue = context.payload.issue;

	const commenter = comment.user?.login || "";
	if (commenter.toLowerCase().endsWith("[bot]")) return;

	const bodyTrim = (comment.body || "").trim();
	if (bodyTrim.toLowerCase() !== "ready") return;

	const owner = context.repo.owner;
	const repo = context.repo.repo;
	const prNumber = issue.number;

	// Get PR details
	const { data: pr } = await github.rest.pulls.get({
		owner,
		repo,
		pull_number: prNumber,
	});

	// Check if PR has pr-not-ready label
	const labels = pr.labels.map(l => l.name);
	if (!labels.includes("pr-not-ready")) return;

	// Get closing issues
	const q = `
		query($owner:String!, $repo:String!, $prNumber:Int!) {
			repository(owner:$owner, name:$repo) {
				pullRequest(number:$prNumber) {
					id
					closingIssuesReferences(first: 10) {
						nodes {
							id
							number
							labels(first: 20) { nodes { name } }
							assignees(first: 50) { nodes { login } }
						}
					}
				}
			}
		}
	`;
	const res = await github.graphql(q, { owner, repo, prNumber });
	const issues = res.repository.pullRequest.closingIssuesReferences.nodes || [];

	// Check if commenter is assigned to all linked issues
	const commenterLower = commenter.toLowerCase();
	const notAssignedTo = issues.filter(iss => {
		const assignees = (iss.assignees.nodes || []).map(a => (a.login || "").toLowerCase());
		return !assignees.includes(commenterLower);
	});

	if (notAssignedTo.length > 0) {
		await addComment(github, context, [
			`Hi @${commenter}, you cannot mark this PR as ready because you are not assigned to its linked issues.`,
			"",
			"Not assigned to: " + notAssignedTo.map(i => `#${i.number}`).join(", "),
			`If you think this is a mistake, please ping ${pingInstructor()}.`,
		].join("\n"));
		return;
	}

	const txt = requireTeamsFile(core);
	if (!txt) return;
	const idx = parseTeams(txt);
	const team = getTeamMembersOrNull(idx, commenter);

	if (!team) {
		await addComment(github, context, [
			`Hi @${commenter}, I could not find your GitHub user in any team.`,
			"",
			`Please make sure you are logged in with the correct GitHub account. If you believe this is an error, please ping ${pingInstructor()}.`,
		].join("\n"));
		return;
	}

	// Compute required budget from linked issues' story point labels
	const requiredBudget = issues.reduce((sum, iss) => {
		const spLabel = (iss.labels?.nodes || []).find(l => ALL_STORY_POINTS_LABELS.includes(l.name));
		return sum + (spLabel ? (getPointsFromLabel(spLabel.name) ?? 0) : 0);
	}, 0);

	// Check team budget and find optimal set of tasks to consume
	const availableTasks = await fetchTeamBudgetTasks(github, team);
	const totalBudget = availableTasks.reduce((s, t) => s + t.points, 0);
	const optimalTasks = getBudgetTasks(requiredBudget, availableTasks);

	if (optimalTasks === null) {
		await addComment(github, context, [
			`Hi @${commenter}, you cannot mark this PR as ready because your team doesn't have enough budget.`,
			"",
			`Your team has a budget of **${totalBudget} story point${totalBudget !== 1 ? "s" : ""}** available (🪙 tasks), but this PR requires **${requiredBudget} story point${requiredBudget !== 1 ? "s" : ""}**.`,
			"",
			"Please create new issues to increase your team's budget, then comment `ready` again.",
		].join("\n"));
		return;
	}

	const instructor = instructorLogin();

	// Remove pr-not-ready label
	await removeLabelSafe(github, context, "pr-not-ready");

	// Request instructor reviewer
	if (instructor) {
		try {
			await github.rest.pulls.requestReviewers({
				owner,
				repo,
				pull_number: prNumber,
				reviewers: [instructor],
			});
		} catch (e) {
			core.warning(`Failed to request review from instructor: ${e.message}`);
		}
	}

	// Move linked issues to "in review"
	const project = await getSingleRepoProjectV2(github, context);
	const status = await getStatusFieldConfig(github, project.id);
	const inReviewId = findSingleStatusOptionId(status.options, "in review");

	for (const iss of issues) {
		let itemId = await getProjectItemIdForIssue(github, iss.id, project.id);
		if (!itemId) itemId = await addIssueToProject(github, project.id, iss.id);
		await setProjectItemSingleSelect(github, project.id, itemId, status.fieldId, inReviewId);
	}

	for (const task of optimalTasks) {
		await removeLabelFromIssue(github, task.owner, task.repo, task.number, COIN_LABEL);
	}

	const consumedBudget = optimalTasks.reduce((s, t) => s + t.points, 0);
	const consumedLinks = optimalTasks
		.map(t => `[${t.repo}#${t.number}](https://github.com/${t.owner}/${t.repo}/issues/${t.number})`)
		.join(", ");

	await addComment(github, context,
		optimalTasks.length > 0
			? `This PR is now marked as **ready to be merged**.\n\nConsumed ${consumedBudget} 🪙 from: ${consumedLinks}.`
			: "This PR is now marked as **ready to be merged**."
	);
}

module.exports = {
	handleIssueOpened,
	handleIssueEdited,
	handleInboxUnlabeled,
	handleIssueCommentCreated,
	handlePullRequestCommentCreated,
	handleIssueClosed,
};
